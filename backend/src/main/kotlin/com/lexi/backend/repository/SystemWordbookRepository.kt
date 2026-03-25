package com.lexi.backend.repository

import com.lexi.backend.entity.SystemWordbook
import org.springframework.data.jpa.repository.JpaRepository

interface SystemWordbookRepository : JpaRepository<SystemWordbook, Int>
