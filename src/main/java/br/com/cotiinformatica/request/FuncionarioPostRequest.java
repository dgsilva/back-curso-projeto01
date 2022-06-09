package br.com.cotiinformatica.request;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FuncionarioPostRequest {

	private String nome;
	private String matricula;
	private String cpf;
	private Date dataAdmissao;
	private Integer idEmpresa;
}
