package com.sinabro.backend.repository.user;

import com.sinabro.backend.entity.user.Character;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterRepository extends JpaRepository<Character, String> {
}
