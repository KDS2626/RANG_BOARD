package com.study.board.service;

import com.study.board.entity.Board;
import com.study.board.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    // 글 작성 처리
    public void write(Board board, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String projectPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\files";

            UUID uuid = UUID.randomUUID();
            String fileName = uuid + "_" + file.getOriginalFilename();

            File saveFile = new File(projectPath, fileName);
            file.transferTo(saveFile);

            board.setFilename(fileName);
            board.setFilepath("/files/" + fileName);
        }

        boardRepository.save(board);
    }

    // 게시글 수정 처리
    public void update(Board board, MultipartFile file, boolean deleteFile) throws IOException {
        if (deleteFile) {
            // 기존 파일 삭제 처리
            String filePath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\files\\" + board.getFilename();
            File oldFile = new File(filePath);
            if (oldFile.exists()) {
                oldFile.delete();
            }

            board.setFilename(null); // 파일명 초기화
            board.setFilepath(null); // 파일 경로 초기화
        }

        if (file != null && !file.isEmpty()) {
            // 새 파일 업로드 처리
            String projectPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\files";

            UUID uuid = UUID.randomUUID();
            String fileName = uuid + "_" + file.getOriginalFilename();

            File saveFile = new File(projectPath, fileName);
            file.transferTo(saveFile);

            board.setFilename(fileName);
            board.setFilepath("/files/" + fileName);
        }

        boardRepository.save(board);
    }

    // 게시글 리스트 처리 (최신 순 정렬)
    public Page<Board> boardList(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }




    // 게시글 총 개수 처리
    public int countBoard() {
        return (int) boardRepository.count(); // 게시글 총 개수 반환
    }

    // 특정 게시글 불러오기
    public Board boardView(Integer id) {
        return boardRepository.findById(id).orElse(null); // 게시글을 찾을 수 없을 경우 null 반환
    }

    // 게시글 삭제 처리
    public void boardDelete(Integer id) {
        boardRepository.deleteById(id);
    }

    // 파일 없이 게시글 저장
    public void save(Board board) {
        boardRepository.save(board);
    }

    public Page<Board> boardSearchList(String searchKeyword, Pageable pageable) {

        return boardRepository.findByTitleContaining(searchKeyword, pageable);
    }
}

