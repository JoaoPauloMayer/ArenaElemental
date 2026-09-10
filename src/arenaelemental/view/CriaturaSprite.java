package arenaelemental.view;

import arenaelemental.modelo.Aparencia;
import arenaelemental.modelo.Aparencia.Adorno;
import arenaelemental.modelo.Criatura;
import arenaelemental.modelo.EspecieCriatura;

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
 * Desenha a criatura em Java2D, a partir da {@link Aparencia} da especie.
 *
 * <p>Antes o desenho saia do tipo elementar, o que faria duas especies de Fogo
 * aparecerem exatamente iguais. Agora a cor do corpo e o adorno vem da ficha da
 * especie, entao especies do mesmo tipo podem ser distinguidas na tela.
 */
public class CriaturaSprite {

    static void desenhar(Graphics2D g, Criatura criatura, int cx, int cy, int tamanho) {
        if (criatura == null) return;
        desenhar(g, criatura.getAparencia(), cx, cy, tamanho, !criatura.estaViva());
    }

    static void desenhar(Graphics2D g, EspecieCriatura especie, int cx, int cy, int tamanho) {
        if (especie == null) return;
        desenhar(g, especie.getAparencia(), cx, cy, tamanho, false);
    }

    static void desenhar(Graphics2D g, Aparencia aparencia, int cx, int cy, int tamanho, boolean desmaiado) {
        if (aparencia == null) return;
        Object oldHint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color cor = desmaiado ? new Color(160, 160, 165) : aparencia.getCorCorpo();
        int r = tamanho / 2;

        // sombra no chao
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(cx - r + 6, cy + r - 8, tamanho - 12, 18);

        // corpo
        g.setColor(cor);
        g.fillOval(cx - r, cy - r, tamanho, tamanho);

        // contorno duplo: um traco escuro por dentro e um claro por fora.
        // Sobre uma foto de fundo, so o traco escuro sumiria nas partes
        // escuras da imagem e so o claro sumiria nas partes claras.
        g.setColor(new Color(255, 255, 255, 120));
        g.setStroke(new BasicStroke(2f));
        g.drawOval(cx - r - 2, cy - r - 2, tamanho + 4, tamanho + 4);
        g.setColor(cor.darker());
        g.setStroke(new BasicStroke(3f));
        g.drawOval(cx - r, cy - r, tamanho, tamanho);

        // barriga
        g.setColor(new Color(255, 255, 255, 70));
        g.fillOval(cx - r / 2, cy - r / 6, r, (int) (r * 0.9));

        // olhos
        int eyeR = Math.max(6, tamanho / 11);
        int eyeY = cy - r / 6;
        g.setColor(Color.WHITE);
        g.fillOval(cx - r / 2, eyeY - eyeR, eyeR * 2, eyeR * 2);
        g.fillOval(cx + r / 2 - eyeR * 2, eyeY - eyeR, eyeR * 2, eyeR * 2);
        int pupilR = Math.max(3, eyeR / 2);
        g.setColor(desmaiado ? new Color(90, 90, 95) : Color.BLACK);
        if (desmaiado) {
            // olhos fechados (X) quando desmaiado
            g.setStroke(new BasicStroke(2.5f));
            drawX(g, cx - r / 2 + eyeR, eyeY, pupilR + 2);
            drawX(g, cx + r / 2 - eyeR, eyeY, pupilR + 2);
        } else {
            g.fillOval(cx - r / 2 + eyeR - pupilR, eyeY - pupilR, pupilR * 2, pupilR * 2);
            g.fillOval(cx + r / 2 - eyeR - pupilR, eyeY - pupilR, pupilR * 2, pupilR * 2);
        }

        if (!desmaiado) desenharAdorno(g, aparencia.getAdorno(), cx, cy, r);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                oldHint == null ? RenderingHints.VALUE_ANTIALIAS_DEFAULT : oldHint);
    }

    private static void desenharAdorno(Graphics2D g, Adorno adorno, int cx, int cy, int r) {
        if (adorno == null) return;
        switch (adorno) {
            case CHAMA:    desenharChama(g, cx, cy - r, r); break;
            case ONDAS:    desenharOndas(g, cx, cy, r); break;
            case FOLHAS:   desenharFolhas(g, cx, cy - r, r); break;
            case ESPINHOS: desenharEspinhos(g, cx, cy - r, r); break;
            case CRISTAL:  desenharCristal(g, cx, cy - r, r); break;
            case CHIFRES:  desenharChifres(g, cx, cy - r, r); break;
            case AUREOLA:  desenharAureola(g, cx, cy - r, r); break;
            case ANTENA:   desenharAntena(g, cx, cy - r, r); break;
            case GALHOS:   desenharGalhos(g, cx, cy - r, r); break;
            case MAGMA:    desenharMagma(g, cx, cy - r, r); break;
            case NENHUM:
            default:       break;
        }
    }

    private static void drawX(Graphics2D g, int cx, int cy, int s) {
        g.drawLine(cx - s, cy - s, cx + s, cy + s);
        g.drawLine(cx - s, cy + s, cx + s, cy - s);
    }

    private static void desenharChama(Graphics2D g, int cx, int topoY, int r) {
        int fw = (int) (r * 0.55);
        int fh = (int) (r * 0.7);
        GeneralPath p = new GeneralPath();
        p.moveTo(cx, topoY - fh);
        p.curveTo(cx + fw, topoY - fh * 0.5, cx + fw * 0.7, topoY, cx, topoY + fh * 0.15);
        p.curveTo(cx - fw * 0.7, topoY, cx - fw, topoY - fh * 0.5, cx, topoY - fh);
        p.closePath();
        g.setColor(new Color(255, 196, 61));
        g.fill(p);
        g.setColor(new Color(230, 92, 46));
        g.setStroke(new BasicStroke(2f));
        g.draw(p);
    }

    private static void desenharOndas(Graphics2D g, int cx, int cy, int r) {
        g.setColor(new Color(255, 255, 255, 160));
        g.setStroke(new BasicStroke(2.5f));
        for (int i = -1; i <= 1; i++) {
            int y = cy + i * (r / 3);
            g.drawArc(cx - r / 3, y, r / 3, r / 6, 200, 140);
            g.drawArc(cx, y, r / 3, r / 6, 200, 140);
        }
    }

    private static void desenharFolhas(Graphics2D g, int cx, int topoY, int r) {
        g.setColor(new Color(140, 210, 110));
        int lw = (int) (r * 0.5), lh = (int) (r * 0.7);
        g.fillOval(cx - lw, topoY - lh, lw, lh);
        g.fillOval(cx, topoY - lh + 6, lw, lh);
        g.setColor(new Color(60, 120, 50));
        g.setStroke(new BasicStroke(1.8f));
        g.drawLine(cx - lw / 2, topoY - lh / 2, cx - lw / 2, topoY - 4);
        g.drawLine(cx + lw / 2, topoY - lh / 2 + 6, cx + lw / 2, topoY + 2);
    }

    private static void desenharEspinhos(Graphics2D g, int cx, int topoY, int r) {
        g.setColor(new Color(120, 108, 96));
        g.setStroke(new BasicStroke(2f));
        int h = (int) (r * 0.45);
        for (int i = -1; i <= 1; i++) {
            int x = cx + i * (r / 2);
            GeneralPath p = new GeneralPath();
            p.moveTo(x - r * 0.16, topoY + 4);
            p.lineTo(x, topoY - h);
            p.lineTo(x + r * 0.16, topoY + 4);
            p.closePath();
            g.setColor(new Color(196, 184, 168));
            g.fill(p);
            g.setColor(new Color(120, 108, 96));
            g.draw(p);
        }
    }

    private static void desenharCristal(Graphics2D g, int cx, int topoY, int r) {
        int w = (int) (r * 0.34), h = (int) (r * 0.68);
        GeneralPath p = new GeneralPath();
        p.moveTo(cx, topoY - h);
        p.lineTo(cx + w, topoY - h * 0.45);
        p.lineTo(cx + w * 0.6, topoY + 6);
        p.lineTo(cx - w * 0.6, topoY + 6);
        p.lineTo(cx - w, topoY - h * 0.45);
        p.closePath();
        g.setColor(new Color(174, 226, 240));
        g.fill(p);
        g.setColor(new Color(96, 168, 196));
        g.setStroke(new BasicStroke(2f));
        g.draw(p);
        g.drawLine(cx, topoY - h, cx, topoY + 6);
    }

    /** Dois chifres curvos, abrindo para fora. */
    private static void desenharChifres(Graphics2D g, int cx, int topoY, int r) {
        int h = (int) (r * 0.55);
        for (int lado = -1; lado <= 1; lado += 2) {
            double base = cx + lado * r * 0.42;
            GeneralPath p = new GeneralPath();
            p.moveTo(base - r * 0.14, topoY + r * 0.18);
            p.quadTo(base - lado * r * 0.05, topoY - h * 0.5, base + lado * r * 0.30, topoY - h);
            p.quadTo(base + lado * r * 0.12, topoY - h * 0.35, base + r * 0.14, topoY + r * 0.22);
            p.closePath();
            g.setColor(new Color(52, 40, 78));
            g.fill(p);
            g.setColor(new Color(170, 150, 220));
            g.setStroke(new BasicStroke(1.6f));
            g.draw(p);
        }
    }

    /** Uma haste curva com uma isca luminosa na ponta, como a dos peixes do fundo do mar. */
    private static void desenharAntena(Graphics2D g, int cx, int topoY, int r) {
        double xPonta = cx + r * 0.55, yPonta = topoY - r * 0.55;
        GeneralPath haste = new GeneralPath();
        haste.moveTo(cx - r * 0.05, topoY + r * 0.12);
        haste.curveTo(cx, topoY - r * 0.55, cx + r * 0.35, topoY - r * 0.75, xPonta, yPonta);
        g.setColor(new Color(20, 28, 56));
        g.setStroke(new BasicStroke(Math.max(2f, r * 0.07f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(haste);

        int brilho = Math.max(10, (int) (r * 0.42)), isca = Math.max(6, (int) (r * 0.22));
        g.setPaint(new RadialGradientPaint((float) xPonta, (float) yPonta, brilho,
                new float[] {0f, 1f},
                new Color[] {new Color(170, 255, 230, 150), new Color(170, 255, 230, 0)}));
        g.fillOval((int) (xPonta - brilho), (int) (yPonta - brilho), brilho * 2, brilho * 2);
        g.setColor(new Color(200, 255, 236));
        g.fillOval((int) (xPonta - isca / 2.0), (int) (yPonta - isca / 2.0), isca, isca);
    }

    /** Galhos secos e retorcidos, com uma folha escura em cada ponta. */
    private static void desenharGalhos(Graphics2D g, int cx, int topoY, int r) {
        g.setStroke(new BasicStroke(Math.max(2f, r * 0.08f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int lado = -1; lado <= 1; lado += 2) {
            double base = cx + lado * r * 0.35;
            double xMeio = base + lado * r * 0.12, yMeio = topoY - r * 0.35;
            double xPonta = base + lado * r * 0.42, yPonta = topoY - r * 0.62;
            double xRamo = base - lado * r * 0.10, yRamo = topoY - r * 0.66;

            g.setColor(new Color(58, 42, 30));
            GeneralPath galho = new GeneralPath();
            galho.moveTo(base, topoY + r * 0.15);
            galho.lineTo(xMeio, yMeio);
            galho.lineTo(xPonta, yPonta);
            galho.moveTo(xMeio, yMeio);
            galho.lineTo(xRamo, yRamo);
            g.draw(galho);

            int folha = Math.max(6, (int) (r * 0.2));
            g.setColor(new Color(70, 110, 64));
            g.fillOval((int) (xPonta - folha / 2.0), (int) (yPonta - folha / 2.0), folha, (int) (folha * 0.8));
            g.fillOval((int) (xRamo - folha / 2.0), (int) (yRamo - folha / 2.0), folha, (int) (folha * 0.8));
        }
    }

    /** Pedras de basalto no alto da cabeca, com lava brilhando nas frestas. */
    private static void desenharMagma(Graphics2D g, int cx, int topoY, int r) {
        int[][] pedras = {{-1, 0}, {0, -1}, {1, 0}};
        for (int[] p : pedras) {
            int w = (int) (r * 0.46), h = (int) (r * (p[1] < 0 ? 0.5 : 0.38));
            int x = cx + (int) (p[0] * r * 0.34) - w / 2;
            int y = topoY - h + (int) (r * 0.18) + (p[1] < 0 ? -(int) (r * 0.08) : 0);
            GeneralPath rocha = new GeneralPath();
            rocha.moveTo(x, y + h);
            rocha.lineTo(x + w * 0.12, y + h * 0.3);
            rocha.lineTo(x + w * 0.5, y);
            rocha.lineTo(x + w * 0.9, y + h * 0.25);
            rocha.lineTo(x + w, y + h);
            rocha.closePath();
            g.setColor(new Color(62, 50, 48));
            g.fill(rocha);
            g.setColor(new Color(255, 120, 30));
            g.setStroke(new BasicStroke(Math.max(1.5f, r * 0.04f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x + (int) (w * 0.5), y + (int) (h * 0.2), x + (int) (w * 0.4), y + (int) (h * 0.65));
            g.drawLine(x + (int) (w * 0.4), y + (int) (h * 0.65), x + (int) (w * 0.62), y + h);
            g.setColor(new Color(255, 180, 80, 160));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(rocha);
        }
    }

    /** Um anel de luz flutuando acima da cabeca. */
    private static void desenharAureola(Graphics2D g, int cx, int topoY, int r) {
        int w = (int) (r * 1.1), h = Math.max(6, (int) (r * 0.28));
        int y = topoY - (int) (r * 0.42);
        g.setColor(new Color(255, 230, 120, 90));
        g.setStroke(new BasicStroke(7f));
        g.drawOval(cx - w / 2, y, w, h);
        g.setColor(new Color(255, 206, 60));
        g.setStroke(new BasicStroke(3f));
        g.drawOval(cx - w / 2, y, w, h);
    }
}
