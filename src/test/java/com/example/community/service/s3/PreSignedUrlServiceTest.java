package com.example.community.service.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.community.dto.response.s3.PresignedUrlResponse;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
class PreSignedUrlServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private PreSignedUrlServiceImpl preSignedUrlService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(preSignedUrlService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(preSignedUrlService, "region", "ap-northeast-2");
    }

    @Test
    @DisplayName("presigned-url 발급 - 성공")
    void create_presignedUrl() throws Exception {
        PresignedPutObjectRequest presignedPutObjectRequest = mock(PresignedPutObjectRequest.class);
        when(presignedPutObjectRequest.url()).thenReturn(new URL("https://signed-url"));
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);

        String prefix = "profile";
        String contentType = "image/jpeg";

        PresignedUrlResponse response = preSignedUrlService.createdPresignedUrl(prefix, contentType);

        ArgumentCaptor<PutObjectPresignRequest> presignRequestCaptor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(presignRequestCaptor.capture());

        PutObjectRequest capturedRequest = presignRequestCaptor.getValue().putObjectRequest();

        assertThat(capturedRequest.bucket()).isEqualTo("test-bucket");
        assertThat(capturedRequest.contentType()).isEqualTo(contentType);
        assertThat(capturedRequest.key()).startsWith(prefix + "/");

        assertThat(response.getKey()).isEqualTo(capturedRequest.key());
        assertThat(response.getPresignedUrl()).isEqualTo("https://signed-url");
        assertThat(response.getProfileImageUrl())
                .isEqualTo("https://test-bucket.s3.ap-northeast-2.amazonaws.com/" + capturedRequest.key());
    }
}
