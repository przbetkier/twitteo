package dev.przbetkier.twitteo.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.IOException
import javax.annotation.PostConstruct

private val logger = KotlinLogging.logger {}

@Component
class FirebaseInitializer(
    private val firebaseProperties: FirebaseConfigurationProperties
) {

    @PostConstruct
    fun onStart() {
        logger.info { "Initializing Firebase App..." }
        try {
            initializeFirebaseApp()
        } catch (e: IOException) {
            logger.error(e) { "Failed to initialize Firebase!" }
        }
    }

    @Throws(IOException::class)
    private fun initializeFirebaseApp() {
        if (FirebaseApp.getApps() == null || FirebaseApp.getApps().isEmpty()) {
            val configInputStream = RESOURCE
                .replace("{PROJECT_ID}", firebaseProperties.projectId)
                .replace("{PRIVATE_KEY_ID}", firebaseProperties.privateKeyId)
                .replace("{PRIVATE_KEY}", firebaseProperties.privateKey)
                .replace("{CLIENT_EMAIL}", firebaseProperties.clientEmail)
                .replace("{CLIENT_ID}", firebaseProperties.clientId)
                .replace("{CERT_URL}", firebaseProperties.clientX509CertUrl)
                .byteInputStream()
            val credentials: GoogleCredentials = GoogleCredentials.fromStream(configInputStream)
            val options: FirebaseOptions = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build()
            FirebaseApp.initializeApp(options)
        }
    }

    companion object {
        const val RESOURCE = """
            {
              "type": "service_account",
              "project_id": "{PROJECT_ID}",
              "private_key_id": "{PRIVATE_KEY_ID}",
              "private_key": "{PRIVATE_KEY}",
              "client_email": "{CLIENT_EMAIL}",
              "client_id": "{CLIENT_ID}",
              "auth_uri": "https://accounts.google.com/o/oauth2/auth",
              "token_uri": "https://oauth2.googleapis.com/token",
              "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
              "client_x509_cert_url": "{CERT_URL}"
            }
        """
    }
}
