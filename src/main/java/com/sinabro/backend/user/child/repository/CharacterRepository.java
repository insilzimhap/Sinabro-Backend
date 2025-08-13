package com.sinabro.backend.user.child.repository;

import com.sinabro.backend.user.child.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterRepository extends JpaRepository<Character, String> {
}
