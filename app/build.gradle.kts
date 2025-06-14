plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.quanbadulichmietvuon"
    compileSdk = 34

    packagingOptions {
        exclude ("META-INF/DEPENDENCIES")
    }

    defaultConfig {
        applicationId = "com.example.quanbadulichmietvuon"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation ("androidx.core:core:1.10.1")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.google.firebase:firebase-firestore:25.1.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.cardview:cardview:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("com.google.firebase:firebase-appcheck:16.0.0")
    implementation ("com.google.firebase:firebase-appcheck-safetynet:16.0.0")
    implementation ("com.google.firebase:firebase-appcheck-playintegrity:16.0.0")
    implementation ("me.relex:circleindicator:2.1.6")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.sendgrid:sendgrid-java:4.7.3")


}