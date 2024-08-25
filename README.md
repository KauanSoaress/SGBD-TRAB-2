# Atividades do Trabalho
- modelar entidades:
    - [ ] bloqueios
    - [ ] tabela de compatibilidade
    - [ ] cópia do syslockinfo
    - [ ] escalonamento

- implementar serviços:
    - [ ] ler o input (ler a string e converter em operações de uma transação)
    - [ ] escalonar:
        - [ ] verificar a possibilidade de conceder um bloqueio para aquela operação
        - [ ] algoritmo de embaralhar as transações
        - [ ] atualizar a cópia da syslockinfo
        - [ ] atualizar o grafo de espera e procurar por ciclos
        - [ ] prevenir deadlock (implementar bloqueio do tipo update)
        - [ ] implementar bloqueio de múltipla granulosidade
        - [ ] converter os bloqueios em bloqueios certify
