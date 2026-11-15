package battleship;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

class PDFGeneratorTest {

    private static final Path PDF = Path.of("relatorio_batalha.pdf");

    private Path backup;
    private PrintStream originalOut;
    private ByteArrayOutputStream capturedOut;

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        // guarda o PDF que já existe no projeto, para o repor no fim
        backup = tempDir.resolve("backup.pdf");
        if (Files.exists(PDF)) {
            Files.move(PDF, backup);
        }
        // captura o System.out para verificar as mensagens
        originalOut = System.out;
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void tearDown() throws IOException {
        System.setOut(originalOut);
        Files.deleteIfExists(PDF); // apaga o PDF (ou a pasta) criado pelo teste
        if (Files.exists(backup)) {
            Files.move(backup, PDF, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String lerTextoDoPdf() throws IOException {
        try (PDDocument doc = Loader.loadPDF(PDF.toFile())) {
            return new PDFTextStripper().getText(doc);
        }
    }

    @Test
    @DisplayName("Cria o ficheiro PDF não vazio")
    void testCriaFicheiro() throws IOException {
        PDFGenerator.criarRelatorioBatalha(10, 2);

        assertTrue(Files.exists(PDF));
        assertTrue(Files.size(PDF) > 0);
    }

    @Test
    @DisplayName("O PDF tem uma página com o título e as estatísticas")
    void testConteudoDoPdf() throws IOException {
        PDFGenerator.criarRelatorioBatalha(7, 3);

        try (PDDocument doc = Loader.loadPDF(PDF.toFile())) {
            assertEquals(1, doc.getNumberOfPages());
        }
        String texto = lerTextoDoPdf();
        assertTrue(texto.contains("Relatório Tático - Batalha Naval"));
        assertTrue(texto.contains("Total de Tiros Disparados: 7"));
        assertTrue(texto.contains("Navios Afundados: 3"));
    }

    @Test
    @DisplayName("Aceita estatísticas a zero")
    void testValoresZero() throws IOException {
        PDFGenerator.criarRelatorioBatalha(0, 0);

        String texto = lerTextoDoPdf();
        assertTrue(texto.contains("Total de Tiros Disparados: 0"));
        assertTrue(texto.contains("Navios Afundados: 0"));
    }

    @Test
    @DisplayName("Escreve a mensagem de sucesso na consola")
    void testMensagemDeSucesso() {
        PDFGenerator.criarRelatorioBatalha(5, 1);

        assertTrue(capturedOut.toString()
                .contains("Ficheiro PDF gerado com sucesso: relatorio_batalha.pdf"));
    }

    @Test
    @DisplayName("Substitui o PDF anterior numa segunda chamada")
    void testSobrescreveFicheiro() throws IOException {
        PDFGenerator.criarRelatorioBatalha(1, 0);
        PDFGenerator.criarRelatorioBatalha(99, 4);

        String texto = lerTextoDoPdf();
        assertTrue(texto.contains("Total de Tiros Disparados: 99"));
        assertFalse(texto.contains("Total de Tiros Disparados: 1\n"));
    }

    @Test
    @DisplayName("Trata a IOException quando não consegue guardar o ficheiro")
    void testErroAoGuardar() throws IOException {
        // uma pasta com o nome do ficheiro faz o save() falhar
        Files.createDirectory(PDF);

        assertDoesNotThrow(() -> PDFGenerator.criarRelatorioBatalha(5, 1));

        assertTrue(capturedOut.toString()
                .contains("Ocorreu um erro ao gerar o ficheiro PDF"));
        assertTrue(Files.isDirectory(PDF));
    }
}