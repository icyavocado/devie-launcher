package com.ovacoda.devie;

/* REWORK(github): public-release feedback targets. The original app pointed
   these at Reddit/Play Store/email; the rework points everything at the
   GitHub repo. Owner comes from the git origin remote once the repo is
   pushed — adjust REPO_URL here if the remote moves. */
public final class DevieLinks {
    public static final String REPO_URL = "https://github.com/icyavocado/devie-launcher";
    public static final String ISSUES_URL = REPO_URL + "/issues";

    private DevieLinks() {
    }
}