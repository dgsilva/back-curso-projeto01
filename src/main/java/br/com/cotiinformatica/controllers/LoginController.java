package br.com.cotiinformatica.controllers;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.cotiinformatica.cryptography.MD5Cryptography;
import br.com.cotiinformatica.entities.Usuario;
import br.com.cotiinformatica.repositories.IUsuarioRepository;
import br.com.cotiinformatica.request.LoginPostRequest;
import br.com.cotiinformatica.responses.LoginResponse;
import br.com.cotiinformatica.security.JwtSecurity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Controller
@CrossOrigin(origins = "http://localhost:4200/")
public class LoginController {

	private static final String ENDPOINT = "api/login";

	@Autowired
	private IUsuarioRepository usuarioRepository;

	@CrossOrigin
	@RequestMapping(value = ENDPOINT, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<LoginResponse> post(@RequestBody LoginPostRequest request) {

		try {

			LoginResponse response = new LoginResponse();

			// consultar o usuario no banco de dados atraves do email e da senha
			Usuario usuario = usuarioRepository.findByEmailAndSenha(request.getEmail(),
					MD5Cryptography.encrypt(request.getSenha()));

			// verificar se o usuário foi encontrado
			if (usuario != null) {

				response.setStatusCode(200); // sucesso!
				response.setMensagem("Usuário autenticado com sucesso.");
				response.setAccessToken(getAccessToken(usuario.getEmail()));
				response.setNomeUsuario(usuario.getNome());
				response.setEmailUsuario(usuario.getEmail());

				return ResponseEntity.status(HttpStatus.OK).body(response);
			} else {

				response.setStatusCode(401); // acesso negado!
				response.setMensagem("Acesso negado.");

				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	// método para gerar o ACCESS TOKEN do usuário
	private String getAccessToken(String emailUsuario) {

		List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");

		// COTI_JWT -> nome da aplicação que gerou o token!
		String token = Jwts.builder().setId("COTI_JWT").setSubject(emailUsuario)
				.claim("authorities",
						grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 6000000))
				.signWith(SignatureAlgorithm.HS512, JwtSecurity.SECRET.getBytes()).compact();

		return token;

	}

}
