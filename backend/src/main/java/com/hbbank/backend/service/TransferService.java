package com.hbbank.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hbbank.backend.dto.TransferRequestDTO;

import lombok.extern.slf4j.Slf4j;

@Service
// @RequiredArgsConstructor
@Transactional
@Slf4j
public class TransferService {

    public boolean executeTransfer(TransferRequestDTO dto) {
        return true;
    }

}
