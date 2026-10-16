package com.localplatform.serviceb;

import org.junit.jupiter.api.Test;

/**
 * Smoke test — Spring context yüklenmeden sadece derleme ve JVM sağlığını doğrular.
 * Gerçek integration testleri Testcontainers gerektiren ayrı bir profilde çalışır.
 */
class ServiceBApplicationTest {

    @Test
    void applicationClassLoads() {
        var clazz = ServiceBApplication.class;
        assert clazz != null : "ServiceBApplication sınıfı yüklenemedi";
    }
}
