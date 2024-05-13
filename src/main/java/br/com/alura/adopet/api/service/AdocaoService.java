package br.com.alura.adopet.api.service;

import br.com.alura.adopet.api.controller.dto.ReprovacaoAdocaoDto;
import br.com.alura.adopet.api.controller.dto.AprovacaoAdocaoDto;
import br.com.alura.adopet.api.controller.dto.SolicitacaoAdocaoDto;
import br.com.alura.adopet.api.model.Adocao;
import br.com.alura.adopet.api.model.Pet;
import br.com.alura.adopet.api.model.Tutor;
import br.com.alura.adopet.api.repository.AdocaoRepository;
import br.com.alura.adopet.api.repository.PetRepository;
import br.com.alura.adopet.api.repository.TutorRepository;
import br.com.alura.adopet.api.validation.ValidacaoSolicitacaoAdocao;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AdocaoService {

    @Autowired
    private AdocaoRepository adocaoRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private List<ValidacaoSolicitacaoAdocao> validacoes;

    public void solicitar(@Valid SolicitacaoAdocaoDto dto){
        Pet pet = petRepository.getReferenceById(dto.idPet());
        Tutor tutor = tutorRepository.getReferenceById(dto.idTutor());

        validacoes.forEach(v -> v.validar(dto));

        Adocao adocao = new Adocao(tutor, pet, dto.motivo());

        adocaoRepository.save(adocao);

        String to = adocao.getPet().getAbrigo().getEmail();
        String subject = "Solicitação de adoção";
        String message = "Olá " +adocao.getPet().getAbrigo().getNome() +
                        "!\n\nUma solicitação de adoção foi registrada hoje para o pet: " +
                        adocao.getPet().getNome() +
                        ". \nFavor avaliar para aprovação ou reprovação.";

        emailService.enviarEmail(to, subject, message);
    }

    public void aprovar(@Valid AprovacaoAdocaoDto dto){
        Adocao adocao = adocaoRepository.getReferenceById(dto.idAdocao());
        adocao.marcarComoAprovado();

        String to = adocao.getPet().getAbrigo().getEmail();
        String subject = "Adoção aprovada";
        String message = "Parabéns " + adocao.getTutor().getNome() +
                        "!\n\nSua adoção do pet " +
                        adocao.getPet().getNome() +", solicitada em " +
                        adocao.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +
                        ", foi aprovada.\nFavor entrar em contato com o abrigo " +
                        adocao.getPet().getAbrigo().getNome() +
                        " para agendar a busca do seu pet.";

        emailService.enviarEmail(to, subject, message);

    }

    public void reprovar(@Valid ReprovacaoAdocaoDto dto){
        Adocao adocao = adocaoRepository.getReferenceById(dto.idAdocao());
        adocao.marcarComoReprovado(dto.justificativa());

        String to = "";
        String subject = "";
        String message = "Olá " +adocao.getTutor().getNome() +
                        "!\n\nInfelizmente sua adoção do pet " +
                        adocao.getPet().getNome() +
                        ", solicitada em " +
                        adocao.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +
                        ", foi reprovada pelo abrigo " +
                        adocao.getPet().getAbrigo().getNome() +
                        " com a seguinte justificativa: " +
                        adocao.getJustificativaStatus();

        emailService.enviarEmail(to, subject, message);

    }
}
