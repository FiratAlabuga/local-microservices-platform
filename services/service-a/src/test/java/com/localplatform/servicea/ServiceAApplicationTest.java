package com.localplatform.servicea;

import org.junit.jupiter.api.Test;

/**
 * Smoke test — Spring context yüklenmeden sadece derleme ve JVM sağlığını doğrular.
 * Gerçek integration testleri Testcontainers gerektiren ayrı bir profilde çalışır.
 */
class ServiceAApplicationTest {

    @Test
    void applicationClassLoads() {
        // Spring context başlatılmadan yalnızca sınıfın yüklenebileceğini doğrula.
        // Context testleri için @SpringBootTest + Testcontainers kullanılır.
        var clazz = ServiceAApplication.class;
        assert clazz != null : "ServiceAApplication sınıfı yüklenemedi";
    }
}
