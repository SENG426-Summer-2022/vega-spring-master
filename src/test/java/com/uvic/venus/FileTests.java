package com.uvic.venus;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import com.uvic.venus.controller.FileController;
import com.uvic.venus.storage.StorageService;

@SpringBootTest
public class FileTests {

    @InjectMocks
    FileController fileController;

    @Mock
    StorageService storageService;
}
