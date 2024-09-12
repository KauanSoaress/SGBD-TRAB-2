# Atividades do Trabalho
- modelar entidades:
    - [x] bloqueios
    - [x] tabela de compatibilidade
    - [ ] cópia do syslockinfo
    - [ ] escalonamento

- implementar serviços:
    - [x] ler o input (ler a string e converter em operações de uma transação)
    - [ ] escalonar:
        - [x] verificar a possibilidade de conceder um bloqueio para aquela operação
        - [ ] algoritmo de embaralhar as transações
        - [ ] atualizar a cópia da syslockinfo
        - [x] atualizar o grafo de espera e procurar por ciclos
        - [ ] prevenir deadlock (implementar bloqueio do tipo update)
        - [ ] implementar bloqueio de múltipla granulosidade
        - [ ] converter os bloqueios em bloqueios certify
