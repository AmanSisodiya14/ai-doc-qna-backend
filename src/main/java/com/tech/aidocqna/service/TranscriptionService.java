package com.tech.aidocqna.service;

import com.tech.aidocqna.dto.internal.TranscriptionResult;

import java.io.File;

public interface TranscriptionService {

    TranscriptionResult transcribe(File file);
}
