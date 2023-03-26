package com.mybox.mybox.folder.controller;

import com.mybox.mybox.folder.service.FolderService;
import com.mybox.mybox.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public void createFolder(@AuthenticationPrincipal User user, @RequestBody FolderRequestDto requestDto) {
        folderService.createFolder(user.getHomeFolder() + requestDto.getFolderName());
    }

}