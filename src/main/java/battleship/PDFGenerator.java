package battleship;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;

public class PDFGenerator {

    // Metodo que recebe as estatísticas do jogo e gera o PDF
    public static void criarRelatorioBatalha(int totalTiros, int naviosAfundados) {

        // 1. Instancia um novo documento em branco
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            // 2. Prepara a "caneta" para escrever no documento
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                // Título
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.newLineAtOffset(50, 700); // Posição (X, Y) na página
                contentStream.showText("Relatório Tático - Batalha Naval");
                contentStream.endText();

                // Estatísticas
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 650);
                contentStream.showText("Total de Tiros Disparados: " + totalTiros);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 630);
                contentStream.showText("Navios Afundados: " + naviosAfundados);
                contentStream.endText();
            }

            // 3. Guarda o ficheiro na pasta do projeto
            document.save("relatorio_batalha.pdf");
            System.out.println("-> Ficheiro PDF gerado com sucesso: relatorio_batalha.pdf");

        } catch (IOException e) {
            System.out.println("Ocorreu um erro ao gerar o ficheiro PDF: " + e.getMessage());
        }
    }
}