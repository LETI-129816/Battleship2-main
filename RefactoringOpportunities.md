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
| PDFGenerator::criarRelatorioBatalha (bloco try do PDPageContentStream) | Long Method | Extract Method | 129771 |
| PDFGenerator::criarRelatorioBatalha (literal "relatorio_batalha.pdf") | Duplicated Code (literal repetido) | Introduce Constant | 129771 |
| PDFGenerator::escreverConteudo (expressão `new PDType1Font(HELVETICA)` repetida) | Duplicated Code | Introduce Variable | 129771 |
| Move::processEnemyFire (variável `output`) | Nome pouco descritivo | Rename | 129771 |
| Move::processEnemyFire (ObjectMapper criado em cada chamada) | Objeto recriado a cada execução | Introduce Field | 129771 |
| Game::fireShots (alreadyShot) | Nome pouco descritivo | Rename | 129802 |
| Game::getRemainingShips (floatingShips) | Variável temporária redundante | Inline Variable | 129802 |
| Game::readEnemyFire ("[A-Za-z]") | Literal sem nome explicativo | Introduce Constant | 129802 |
| Game::printBoardShots (condição de água ou posição adjacente) | Expressão condicional complexa | Introduce Variable | 129802 |
| Game::printBoard (impressão da legenda) | Método com várias responsabilidades | Extract Method | 129802 |