package com.study.board.controller;

import com.study.board.entity.Board;
import com.study.board.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
public class BoardController {

    @Autowired
    private BoardService boardService;

    @GetMapping("/board/write")
    public String boardWriteForm() {
        return "boardwrite";
    }

    @PostMapping("/board/writepro")
    public String boardWritePro(Board board, Model model, @RequestParam("file") MultipartFile file) throws Exception {
        boardService.write(board, file);

        model.addAttribute("message", "글 작성이 완료 되었습니다.");
        model.addAttribute("searchUrl", "/board/list");

        return "message";
    }

    @GetMapping("/board/list")
    public String boardList(Model model,
                            @RequestParam(defaultValue = "0", name="page") int page,
                            @RequestParam(name="searchKeyword", required = false) String searchKeyword,
                            Pageable pageable) {
        // 페이지 번호가 0 미만이면 0으로 설정
        if (page < 0) {
            page = 0;
        }

        int pageSize = 10;  // 한 페이지에 보여줄 게시글 수

        // Pageable 객체를 이용해 자동 페이지 처리 및 최신 순 정렬
        pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Order.desc("id")));

        // 검색어가 없으면 모든 게시글을 불러오고, 검색어가 있으면 검색된 게시글만 불러옴
        Page<Board> boardPage;
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            boardPage = boardService.boardList(pageable);
        } else {
            boardPage = boardService.boardSearchList(searchKeyword, pageable);
        }

        // 총 페이지 수 계산
        int totalPage = boardPage.getTotalPages();
        int startPage = (page / 10) * 10 + 1;  // 1, 11, 21, ...
        int endPage = Math.min(startPage + 9, totalPage);  // 10, 20, 30, ...

        // 다음 페이지 번호 계산
        int nextPage = (endPage < totalPage) ? startPage + 10 : totalPage;
        int prevPage = (startPage > 1) ? startPage - 10 : 0;

        model.addAttribute("list", boardPage.getContent());  // 게시글 리스트
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("nowPage", page + 1);  // 현재 페이지
        model.addAttribute("nextPage", nextPage);  // 다음 페이지
        model.addAttribute("prevPage", prevPage);  // 이전 페이지
        model.addAttribute("searchKeyword", searchKeyword); // 검색 키워드

        return "boardlist";
    }



    @GetMapping("/board/view")
    public String boardView(Model model, @RequestParam("id") Integer id) {
        model.addAttribute("board", boardService.boardView(id));
        return "boardview";
    }

    @GetMapping("/board/delete")
    public String boardDelete(@RequestParam("id") Integer id) {
        boardService.boardDelete(id);
        return "redirect:/board/list";
    }

    @GetMapping("/board/modify/{id}")
    public String boardModify(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("board", boardService.boardView(id));
        return "boardmodify";
    }

    @PostMapping("/board/update/{id}")
    public String boardUpdate(@PathVariable("id") Integer id, Board board,
                              @RequestParam("file") MultipartFile file,
                              @RequestParam(value = "deleteFile", defaultValue = "false") boolean deleteFile) throws IOException {

        Board existingBoard = boardService.boardView(id);
        if (existingBoard != null) {
            board.setId(id);  // 기존 게시글의 ID를 설정
            boardService.update(board, file, deleteFile);  // 수정 메서드 호출
        }

        return "redirect:/board/list";
    }
}
