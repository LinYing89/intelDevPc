package com.bairock.intelDevPc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bairock.iot.intelDev.data.DragConfig;

public interface DragConfigRepository extends JpaRepository<DragConfig, String> {

    DragConfig findByDevGroupId(String devGroupId);
}
