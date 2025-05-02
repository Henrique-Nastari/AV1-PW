package ufrn.br.lojapw.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ufrn.br.lojapw.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Cliente findByEmail(String email);
}
