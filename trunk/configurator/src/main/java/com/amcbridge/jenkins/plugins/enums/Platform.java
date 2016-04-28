package com.amcbridge.jenkins.plugins.enums;

public enum Platform {

    x86("x86"),
    x64("x64"),
    ANY_CPU("Any CPU"),
    WIN_32("Win32"),
    x86_64("x86_64"),
    i386("i386"),
    ppc("ppc"),
    mixedPlatforms("Mixed Platforms");

    private String platformValue;

    private Platform(String value) {
        this.platformValue = value;
    }

    @Override
    public String toString() {
        return platformValue;
    }
}
