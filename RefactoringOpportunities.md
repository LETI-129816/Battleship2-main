# Refactoring Opportunities

| Local | Code smell | Refabricação | Nº aluno |
|---|---|---|---|
| Tasks::menu (variável `naviosAfundados`, em rajada e simula) | Variável temporária redundante | Inline Variable | 129816 |
| Tasks::menu (bloco game.over() / PDFGenerator / System.exit, em rajada e simula) | Duplicated Code | Extract Method | 129816 |
| Tasks::menu (literal "tipo_navio_exato" repetido) | Duplicated Code (literal repetido) | Introduce Constant | 129816 |
| Tasks::menu (expressão `src.getClass()` repetida nos serializadores Gson) | Duplicated Code | Introduce Variable | 129816 |
| Tasks::menu (case GUARDAR) | Long Method | Extract Method | 129816 |
| Tasks::menu (construção do Gson em CARREGAR) | Long Method | Extract Method | 129816 |
| Tasks::metodoComCheiro | Speculative Generality (código sem uso) | Safe Delete | 129816 |
| Tasks::menu (variável `in`) | Nome pouco descritivo | Rename | 129816 |