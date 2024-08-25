package com.bytethrasher.turbine.url;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class URLParserTest {

    private URLParser underTest = new URLParser();

    @Test
    void testSpaceBeginning() {
        final String result = underTest.parseDomain(" http://www.example.com");

        assertEquals("www.example.com", result);
    }

    @Test
    void testSpaceEnding() {
        final String result = underTest.parseDomain("http://www.example.com ");

        assertEquals("www.example.com", result);
    }

    @Test
    void testStartsWithHttp() {
        final String result = underTest.parseDomain("http://www.example.com");

        assertEquals("www.example.com", result);
    }

    @Test
    void testStartsWithHttps() {
        final String result = underTest.parseDomain("https://www.example.com");

        assertEquals("www.example.com", result);
    }

    @Test
    void testStartsWithFile() {
        final String result = underTest.parseDomain("file://www.example.com");

        assertNull(result);
    }

    @Test
    void testEndsWithPath() {
        final String result = underTest.parseDomain("http://www.example.com/test/path");

        assertEquals("www.example.com", result);
    }

    @Test
    void testEndsWithDash() {
        final String result = underTest.parseDomain("http://www.example.com/");

        assertEquals("www.example.com", result);
    }

    @Test
    void testHasPort() {
        final String result = underTest.parseDomain("http://www.example.com:8080/asd/asdf");

        assertEquals("www.example.com", result);
    }

    @Test
    void testHasIp() {
        final String result = underTest.parseDomain("http://127.0.0.1:8080/asd/asdf");

        assertEquals("127.0.0.1", result);
    }

    @Test
    void testHasStrangeCharacters() {
        final String result = underTest.parseDomain("http://www.examplé.com/asd/asdf");

        assertEquals("www.examplé.com", result);
    }

    @Test
    void testUppercaseSchema() {
        final String result = underTest.parseDomain("HTTP://www.example.com/asd/asdf");

        assertEquals("www.example.com", result);
    }

    @Test
    void testInvalidDomain1() {
        final String result = underTest.parseDomain("httpfoo/bar");

        assertNull(result);
    }

    @Test
    void testInvalidDomain2() {
        final String result = underTest.parseDomain("//example.com/");

        assertNull(result);
    }

    @Test
    void testInvalidDomain3() {
        final String result = underTest.parseDomain("www/foo");

        assertNull(result);
    }

    @Test
    void testInvalidDomain4() {
        final String result = underTest.parseDomain("http:////www.example.com/198zLrh28");

        assertNull(result);
    }
}
