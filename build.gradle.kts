// Top-level build file where you can add configuration options common to all sub-projects/modules.
@file:Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.kapt) apply false

}

buildscript {
    dependencies {
        classpath (libs.secrets.gradle.plugin)
    }
}