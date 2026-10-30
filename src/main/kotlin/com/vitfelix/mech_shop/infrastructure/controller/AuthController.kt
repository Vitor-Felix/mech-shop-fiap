package com.vitfelix.mech_shop.infrastructure.controller

import com.vitfelix.mech_shop.infrastructure.security.TokenService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.MessageDigest

// DTOs simples para o login (podem ficar aqui no mesmo arquivo para agilizar)
data class LoginRequest(val login: String, val pass: String)
data class LoginResponse(val token: String)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val tokenService: TokenService,
    @Value("\${security.admin.username}") private val adminUsername: String,
    @Value("\${security.admin.password}") private val adminPassword: String
) {
    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<LoginResponse> {
        val validUsername = adminUsername.isNotBlank() &&
            MessageDigest.isEqual(req.login.toByteArray(), adminUsername.toByteArray())
        val validPassword = adminPassword.isNotBlank() &&
            MessageDigest.isEqual(req.pass.toByteArray(), adminPassword.toByteArray())
        if (validUsername && validPassword) {
            val token = tokenService.generateToken(adminUsername)
            return ResponseEntity.ok(LoginResponse(token))
        }
        return ResponseEntity.status(401).build()
    }
}
