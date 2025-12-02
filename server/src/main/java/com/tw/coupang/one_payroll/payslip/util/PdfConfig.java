package com.tw.coupang.one_payroll.payslip.util;

import com.openhtmltopdf.extend.FSCacheEx;
import com.openhtmltopdf.extend.FSCacheValue;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.tw.coupang.one_payroll.payslip.exception.CacheLoadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class PdfConfig {

    private final FSCacheEx<String, FSCacheValue> fontMetricsCache = new InMemoryFSCache<>();

    public PdfRendererBuilder applyBaseConfig(PdfRendererBuilder builder) {
        builder.useCacheStore(
                PdfRendererBuilder.CacheStore.PDF_FONT_METRICS,
                fontMetricsCache
        );
        builder.useFastMode();

        builder.useFont(
                () -> PdfConfig.class.getResourceAsStream("/fonts/NotoSans-Regular.ttf"),
                "NotoSans",
                400,
                BaseRendererBuilder.FontStyle.NORMAL,
                true
        );
        builder.useFont(
                () -> PdfConfig.class.getResourceAsStream("/fonts/NotoSans-Bold.ttf"),
                "NotoSans",
                700,
                BaseRendererBuilder.FontStyle.NORMAL,
                true
        );
        return builder;
    }

    private static class InMemoryFSCache<K, V extends FSCacheValue> implements FSCacheEx<K, V> {

        private final ConcurrentHashMap<K, V> store = new ConcurrentHashMap<>();

        @Override
        public V get(K key) {
            return store.get(key);
        }

        @Override
        public void put(K key, V value) {
            store.put(key, value);
        }

        @Override
        public V get(K key, Callable<? extends V> valueLoader) {
            return store.computeIfAbsent(key, k -> {
                try {
                    return valueLoader.call();
                } catch (Exception e) {
                    throw new CacheLoadException("Failed to load cache value for key: " + key, e);
                }
            });
        }
    }
}
