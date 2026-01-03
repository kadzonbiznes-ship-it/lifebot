/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.ui.overlay;

import imgui.ImGui;
import imgui.ImVec4;
import lombok.Generated;
import org.pbrands.ui.overlay.LifebotTheme;

public class TutorialWindow {
    private static final ImVec4 COLOR_ACCENT_PRIMARY = LifebotTheme.ACCENT_PRIMARY;
    private static final ImVec4 COLOR_ACCENT_GREEN = LifebotTheme.SUCCESS;
    private static final ImVec4 COLOR_ACCENT_YELLOW = LifebotTheme.WARNING;
    private static final ImVec4 COLOR_ACCENT_RED = LifebotTheme.ERROR;
    private static final ImVec4 COLOR_TEXT_PRIMARY = LifebotTheme.TEXT_PRIMARY;
    private static final ImVec4 COLOR_TEXT_SECONDARY = LifebotTheme.TEXT_SECONDARY;
    private static final ImVec4 COLOR_BG = LifebotTheme.BG_DARK;
    private boolean visible = false;
    private int currentPage = 0;
    private static final int TOTAL_PAGES = 5;
    private TutorialCallback callback;

    public void render() {
        if (!this.visible) {
            return;
        }
        float windowWidth = 600.0f;
        float windowHeight = 650.0f;
        float displayWidth = ImGui.getIO().getDisplaySizeX();
        float displayHeight = ImGui.getIO().getDisplaySizeY();
        ImGui.setNextWindowPos((displayWidth - windowWidth) / 2.0f, (displayHeight - windowHeight) / 2.0f, 1);
        ImGui.setNextWindowSize(windowWidth, windowHeight, 1);
        ImGui.pushStyleColor(2, TutorialWindow.COLOR_BG.x, TutorialWindow.COLOR_BG.y, TutorialWindow.COLOR_BG.z, 0.98f);
        ImGui.pushStyleColor(3, TutorialWindow.COLOR_BG.x, TutorialWindow.COLOR_BG.y, TutorialWindow.COLOR_BG.z, 0.0f);
        int flags = 46;
        if (ImGui.begin("Witaj w LifeBot! \uf3a5##tutorial", flags)) {
            ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_PRIMARY.x, TutorialWindow.COLOR_ACCENT_PRIMARY.y, TutorialWindow.COLOR_ACCENT_PRIMARY.z, 1.0f);
            LifebotTheme.textCentered("Poradnik - Jak zacz\u0105\u0107 kopa\u0107");
            ImGui.popStyleColor();
            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();
            ImGui.pushStyleColor(0, TutorialWindow.COLOR_TEXT_SECONDARY.x, TutorialWindow.COLOR_TEXT_SECONDARY.y, TutorialWindow.COLOR_TEXT_SECONDARY.z, 1.0f);
            LifebotTheme.textCentered(String.format("Strona %d z %d", this.currentPage + 1, 5));
            ImGui.popStyleColor();
            ImGui.spacing();
            switch (this.currentPage) {
                case 0: {
                    this.renderPage1_Introduction();
                    break;
                }
                case 1: {
                    this.renderPage2_Requirements();
                    break;
                }
                case 2: {
                    this.renderPage3_Learning();
                    break;
                }
                case 3: {
                    this.renderPage4_Starting();
                    break;
                }
                case 4: {
                    this.renderPage5_Tips();
                }
            }
            float contentY = ImGui.getCursorPosY();
            float windowY = windowHeight - 70.0f;
            if (contentY < windowY) {
                ImGui.dummy(0.0f, windowY - contentY);
            }
            ImGui.separator();
            ImGui.spacing();
            this.renderNavigationButtons();
        }
        ImGui.end();
        ImGui.popStyleColor(2);
    }

    private void renderPage1_Introduction() {
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_GREEN.x, TutorialWindow.COLOR_ACCENT_GREEN.y, TutorialWindow.COLOR_ACCENT_GREEN.z, 1.0f);
        ImGui.text("\uf05a Czym jest LifeBot?");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.textWrapped("LifeBot to inteligentny bot do kopania w\u0119gla na serwerze 4life. Bot UCZY SI\u0118 od Ciebie - obserwuje jak kopiesz i na\u015bladuje Tw\u00f3j styl gry.");
        ImGui.spacing();
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_YELLOW.x, TutorialWindow.COLOR_ACCENT_YELLOW.y, TutorialWindow.COLOR_ACCENT_YELLOW.z, 1.0f);
        ImGui.text("\uf19d Faza Nauki");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.textWrapped("Na samym pocz\u0105tku musisz nauczy\u0107 bota swojego stylu kopania. Wymagane jest prawid\u0142owe wykopanie 50 razy w\u0119gla bez b\u0142\u0119d\u00f3w.");
        ImGui.spacing();
        ImGui.bulletText("Post\u0119p nauki widzisz w Ustawieniach > Uczenie");
        ImGui.bulletText("Mo\u017cesz te\u017c obserwowa\u0107 post\u0119p w widgecie na ekranie");
        ImGui.bulletText("Po nauczeniu b\u0119dziesz m\u00f3g\u0142 uruchomi\u0107 bota");
        ImGui.spacing();
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_TEXT_SECONDARY.x, TutorialWindow.COLOR_TEXT_SECONDARY.y, TutorialWindow.COLOR_TEXT_SECONDARY.z, 1.0f);
        ImGui.textWrapped("\uf01e Je\u015bli uwa\u017casz, \u017ce za s\u0142abo Ci posz\u0142o podczas uczenia i mog\u0142e\u015b lepiej to zrobi\u0107 - zawsze mo\u017cesz zresetowa\u0107 post\u0119p w Ustawieniach > Uczenie i zacz\u0105\u0107 od pocz\u0105tku.");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_TEXT_SECONDARY.x, TutorialWindow.COLOR_TEXT_SECONDARY.y, TutorialWindow.COLOR_TEXT_SECONDARY.z, 1.0f);
        ImGui.textWrapped("\uf3ed Bot posiada wbudowane zabezpieczenia - wykrywa admin\u00f3w, pe\u0142ny ekwipunek i prywatne wiadomo\u015bci. Zachowanie bota przy wykryciu mo\u017cesz dostosowa\u0107 w Ustawieniach.");
        ImGui.popStyleColor();
    }

    private void renderPage2_Requirements() {
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_GREEN.x, TutorialWindow.COLOR_ACCENT_GREEN.y, TutorialWindow.COLOR_ACCENT_GREEN.z, 1.0f);
        ImGui.text("\uf071 Wymagania");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.textWrapped("Aby bot dzia\u0142a\u0142 prawid\u0142owo, upewnij si\u0119 \u017ce spe\u0142niasz poni\u017csze wymagania:");
        ImGui.spacing();
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_YELLOW.x, TutorialWindow.COLOR_ACCENT_YELLOW.y, TutorialWindow.COLOR_ACCENT_YELLOW.z, 1.0f);
        ImGui.text("\uf108 Ustawienia graficzne:");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.bulletText("Wy\u0142\u0105cz filtry kolor\u00f3w w karcie graficznej (NVIDIA/AMD)");
        ImGui.bulletText("Wy\u0142\u0105cz nak\u0142adki (Discord, GeForce Experience, itp.)");
        ImGui.bulletText("Wy\u0142\u0105cz shadery w grze je\u015bli masz je w\u0142\u0105czone");
        ImGui.bulletText("Nie u\u017cywaj Night Light / f.lux ani podobnych");
        ImGui.spacing();
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_YELLOW.x, TutorialWindow.COLOR_ACCENT_YELLOW.y, TutorialWindow.COLOR_ACCENT_YELLOW.z, 1.0f);
        ImGui.text("\uf1de Skala HUD kopalni:");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.textWrapped("Bot wymaga ustawienia 'Skala HUD kopalni' na x1.0. Mo\u017cesz to zmieni\u0107 w grze: F5 > Ustawienia.");
        ImGui.spacing();
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_YELLOW.x, TutorialWindow.COLOR_ACCENT_YELLOW.y, TutorialWindow.COLOR_ACCENT_YELLOW.z, 1.0f);
        ImGui.text("\uf06e Wykrywanie poziom\u00f3w:");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.textWrapped("Bot wymaga wykrycia 'Tw\u00f3j stopie\u0144' oraz 'Wymagany stopie\u0144' na ekranie. S\u0105 to kluczowe mechanizmy - bez ich wykrycia bot nie wystartuje. Upewnij si\u0119, \u017ce stoisz przy \u015bcianie w\u0119glowej i widzisz te informacje.");
        ImGui.spacing();
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_RED.x, TutorialWindow.COLOR_ACCENT_RED.y, TutorialWindow.COLOR_ACCENT_RED.z, 1.0f);
        ImGui.textWrapped("\uf06a Je\u015bli u\u017cywasz filtr\u00f3w kolor\u00f3w lub shader\u00f3w, bot mo\u017ce \u017ale rozpoznawa\u0107 elementy na ekranie!");
        ImGui.popStyleColor();
    }

    private void renderPage3_Learning() {
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_GREEN.x, TutorialWindow.COLOR_ACCENT_GREEN.y, TutorialWindow.COLOR_ACCENT_GREEN.z, 1.0f);
        ImGui.text("\uf19d Jak przeprowadzi\u0107 nauk\u0119?");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.textWrapped("Nauka jest prosta - wystarczy \u017ce b\u0119dziesz normalnie kopa\u0107 w\u0119giel. Bot w tle obserwuje i uczy si\u0119 Twojego stylu.");
        ImGui.spacing();
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_YELLOW.x, TutorialWindow.COLOR_ACCENT_YELLOW.y, TutorialWindow.COLOR_ACCENT_YELLOW.z, 1.0f);
        ImGui.text("\uf0cb Kroki:");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.indent(15.0f);
        ImGui.textWrapped("1. Podejd\u017a do \u015bciany w\u0119glowej");
        ImGui.spacing();
        ImGui.textWrapped("2. Kop normalnie - 50 prawid\u0142owych kopni\u0119\u0107 bez b\u0142\u0119d\u00f3w");
        ImGui.spacing();
        ImGui.textWrapped("3. Obserwuj post\u0119p w Ustawieniach > Uczenie lub w widgecie");
        ImGui.spacing();
        ImGui.textWrapped("4. Gdy post\u0119p osi\u0105gnie 50/50 - bot b\u0119dzie gotowy!");
        ImGui.unindent(15.0f);
        ImGui.spacing();
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_TEXT_SECONDARY.x, TutorialWindow.COLOR_TEXT_SECONDARY.y, TutorialWindow.COLOR_TEXT_SECONDARY.z, 1.0f);
        ImGui.textWrapped("\uf05a Kop w swoim naturalnym tempie. Bot nauczy si\u0119 dok\u0142adnie tak jak Ty kopiesz - Twoich czas\u00f3w reakcji i przerw.");
        ImGui.popStyleColor();
    }

    private void renderPage4_Starting() {
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_GREEN.x, TutorialWindow.COLOR_ACCENT_GREEN.y, TutorialWindow.COLOR_ACCENT_GREEN.z, 1.0f);
        ImGui.text("\uf04b Uruchamianie Bota");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.textWrapped("Gdy bot zbierze wystarczaj\u0105c\u0105 ilo\u015b\u0107 pr\u00f3bek i wykryje poziomy, mo\u017cesz go uruchomi\u0107.");
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_YELLOW.x, TutorialWindow.COLOR_ACCENT_YELLOW.y, TutorialWindow.COLOR_ACCENT_YELLOW.z, 1.0f);
        ImGui.text("\uf11c Klawisze sterowania:");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.indent(15.0f);
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_PRIMARY.x, TutorialWindow.COLOR_ACCENT_PRIMARY.y, TutorialWindow.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text("END");
        ImGui.popStyleColor();
        ImGui.sameLine();
        ImGui.text("- W\u0142\u0105cz/wy\u0142\u0105cz bota");
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_PRIMARY.x, TutorialWindow.COLOR_ACCENT_PRIMARY.y, TutorialWindow.COLOR_ACCENT_PRIMARY.z, 1.0f);
        ImGui.text("INSERT");
        ImGui.popStyleColor();
        ImGui.sameLine();
        ImGui.text("- Poka\u017c/ukryj interfejs");
        ImGui.unindent(15.0f);
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_YELLOW.x, TutorialWindow.COLOR_ACCENT_YELLOW.y, TutorialWindow.COLOR_ACCENT_YELLOW.z, 1.0f);
        ImGui.text("\uf0cb Jak uruchomi\u0107:");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.indent(15.0f);
        ImGui.textWrapped("1. Sta\u0144 przy \u015bcianie w\u0119glowej (twarz\u0105 do \u015bciany)");
        ImGui.textWrapped("2. Upewnij si\u0119 \u017ce widzisz 'Tw\u00f3j stopie\u0144' i 'Wymagany'");
        ImGui.textWrapped("3. Naci\u015bnij END aby w\u0142\u0105czy\u0107 bota");
        ImGui.textWrapped("4. Naci\u015bnij END ponownie aby zatrzyma\u0107");
        ImGui.unindent(15.0f);
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_RED.x, TutorialWindow.COLOR_ACCENT_RED.y, TutorialWindow.COLOR_ACCENT_RED.z, 1.0f);
        ImGui.textWrapped("\uf023 Bot blokuje wysy\u0142anie END i INSERT do gry ze wzgl\u0119d\u00f3w bezpiecze\u0144stwa. Mo\u017cna to wy\u0142\u0105czy\u0107 w Ustawieniach > Dodatkowe.");
        ImGui.popStyleColor();
    }

    private void renderPage5_Tips() {
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_GREEN.x, TutorialWindow.COLOR_ACCENT_GREEN.y, TutorialWindow.COLOR_ACCENT_GREEN.z, 1.0f);
        ImGui.text("\uf005 Porady i Bezpiecze\u0144stwo");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_YELLOW.x, TutorialWindow.COLOR_ACCENT_YELLOW.y, TutorialWindow.COLOR_ACCENT_YELLOW.z, 1.0f);
        ImGui.text("\uf3ed Detekcje:");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.bulletText("Wykrywanie admina na chacie");
        ImGui.bulletText("Wykrywanie pe\u0142nego ekwipunku");
        ImGui.bulletText("Wykrywanie prywatnych wiadomo\u015bci");
        ImGui.bulletText("Wykrywanie wzmianek o Tobie na chacie");
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_TEXT_SECONDARY.x, TutorialWindow.COLOR_TEXT_SECONDARY.y, TutorialWindow.COLOR_TEXT_SECONDARY.z, 1.0f);
        ImGui.textWrapped("\uf013 Zachowanie bota przy ka\u017cdej detekcji (pauza, d\u017awi\u0119k, powiadomienie) mo\u017cesz dostosowa\u0107 w Ustawieniach > Detekcja.");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_YELLOW.x, TutorialWindow.COLOR_ACCENT_YELLOW.y, TutorialWindow.COLOR_ACCENT_YELLOW.z, 1.0f);
        ImGui.text("\uf11b Minigry:");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.textWrapped("Podczas kopania mo\u017cesz gra\u0107 w minigry stworzone przez nas! Nie wp\u0142ywa to na zachowanie postaci w grze ani na dzia\u0142anie bota.");
        ImGui.spacing();
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_YELLOW.x, TutorialWindow.COLOR_ACCENT_YELLOW.y, TutorialWindow.COLOR_ACCENT_YELLOW.z, 1.0f);
        ImGui.text("\uf013 Ustawienia:");
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.bulletText("Kliknij ikon\u0119 z\u0119batki aby otworzy\u0107 ustawienia");
        ImGui.bulletText("Dostosuj g\u0142o\u015bno\u015b\u0107 powiadomie\u0144 (sekcja D\u017awi\u0119ki)");
        ImGui.bulletText("Skonfiguruj detekcje (sekcja Detekcja)");
        ImGui.spacing();
        ImGui.pushStyleColor(0, TutorialWindow.COLOR_ACCENT_GREEN.x, TutorialWindow.COLOR_ACCENT_GREEN.y, TutorialWindow.COLOR_ACCENT_GREEN.z, 1.0f);
        LifebotTheme.textCentered("\uf058 Powodzenia w kopaniu!");
        ImGui.popStyleColor();
    }

    private void renderNavigationButtons() {
        float buttonWidth = 120.0f;
        float spacing = 20.0f;
        float totalWidth = buttonWidth * 3.0f + spacing * 2.0f;
        float startX = (ImGui.getWindowWidth() - totalWidth) / 2.0f;
        ImGui.setCursorPosX(startX);
        ImGui.beginDisabled(this.currentPage == 0);
        if (ImGui.button("\uf060 Poprzednia", buttonWidth, 30.0f)) {
            --this.currentPage;
        }
        ImGui.endDisabled();
        ImGui.sameLine(0.0f, spacing);
        if (this.currentPage == 4) {
            ImGui.pushStyleColor(21, TutorialWindow.COLOR_ACCENT_GREEN.x, TutorialWindow.COLOR_ACCENT_GREEN.y, TutorialWindow.COLOR_ACCENT_GREEN.z, 1.0f);
            ImGui.pushStyleColor(22, TutorialWindow.COLOR_ACCENT_GREEN.x * 1.2f, TutorialWindow.COLOR_ACCENT_GREEN.y * 1.2f, TutorialWindow.COLOR_ACCENT_GREEN.z * 1.2f, 1.0f);
            if (ImGui.button("\uf00c Zako\u0144cz", buttonWidth, 30.0f)) {
                this.completeTutorial();
            }
            ImGui.popStyleColor(2);
        } else if (ImGui.button("Pomi\u0144", buttonWidth, 30.0f)) {
            this.completeTutorial();
        }
        ImGui.sameLine(0.0f, spacing);
        ImGui.beginDisabled(this.currentPage == 4);
        if (ImGui.button("Nast\u0119pna \uf061", buttonWidth, 30.0f)) {
            ++this.currentPage;
        }
        ImGui.endDisabled();
    }

    private void completeTutorial() {
        this.visible = false;
        if (this.callback != null) {
            this.callback.onTutorialCompleted();
        }
    }

    public void show() {
        this.currentPage = 0;
        this.visible = true;
    }

    @Generated
    public boolean isVisible() {
        return this.visible;
    }

    @Generated
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Generated
    public void setCallback(TutorialCallback callback) {
        this.callback = callback;
    }

    public static interface TutorialCallback {
        public void onTutorialCompleted();
    }
}

