package com.sinabro.backend.lobby.service;

import com.sinabro.backend.user.child.entity.Child;
import com.sinabro.backend.user.child.repository.ChildRepository;
import com.sinabro.backend.user.parent.entity.User;
import com.sinabro.backend.user.parent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParentLobbyService {

    private final UserRepository userRepository;
    private final ChildRepository childRepository;

    public String getParentName(String userId) {
        User u = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("부모를 찾을 수 없습니다."));
        return u.getUserName();
    }

    public List<Child> getChildren(String userId) {
        return childRepository.findByParent_UserId(userId);
    }
}
