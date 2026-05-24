package util;

import javax.swing.*;
<<<<<<< HEAD
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * UIHelper — Central design system for the Inventory & Customer Intelligence System.
 * All color tokens, fonts, and component factories live here so every
 * panel stays visually consistent.
 *
 * Redesigned for modern enterprise desktop appearance.
 */
public class UIHelper {

    // =========================================================================
    // COLOR PALETTE
    // =========================================================================
    public static final Color PRIMARY_COLOR    = new Color(59,  130, 246);  // Blue-500
    public static final Color PRIMARY_DARK     = new Color(37,   99, 235);  // Blue-600
    public static final Color SECONDARY_COLOR  = new Color(99,  102, 241);  // Indigo-500
    public static final Color SUCCESS_COLOR    = new Color(34,  197,  94);  // Green-500
    public static final Color SUCCESS_DARK     = new Color(22,  163,  74);  // Green-600
    public static final Color DANGER_COLOR     = new Color(239,  68,  68);  // Red-500
    public static final Color DANGER_DARK      = new Color(220,  38,  38);  // Red-600
    public static final Color WARNING_COLOR    = new Color(251, 146,  60);  // Orange-400
    public static final Color INFO_COLOR       = new Color(14,  165, 233);  // Cyan-500
    public static final Color PURPLE_COLOR     = new Color(168,  85, 247);  // Violet-500
    public static final Color TEAL_COLOR       = new Color(20,  184, 166);  // Teal-500

    // Light tints for badge backgrounds
    public static final Color SUCCESS_LIGHT    = new Color(220, 252, 231);  // Green-100
    public static final Color DANGER_LIGHT     = new Color(254, 226, 226);  // Red-100
    public static final Color WARNING_LIGHT    = new Color(255, 237, 213);  // Orange-100
    public static final Color INFO_LIGHT       = new Color(224, 242, 254);  // Cyan-100
    public static final Color PRIMARY_LIGHT    = new Color(219, 234, 254);  // Blue-100
    public static final Color PURPLE_LIGHT     = new Color(243, 232, 255);  // Violet-100

    // Layout backgrounds
    public static final Color SIDEBAR_BG      = new Color(15,  23,  42);  // Slate-900
    public static final Color SIDEBAR_HOVER   = new Color(30,  41,  59);  // Slate-800
    public static final Color SIDEBAR_SECTION = new Color(71,  85, 105);  // Slate-500
    public static final Color CONTENT_BG      = new Color(241, 245, 249); // Slate-100
    public static final Color CARD_BG         = Color.WHITE;
    public static final Color TEXT_PRIMARY    = new Color(15,  23,  42);  // Slate-900
    public static final Color TEXT_SECONDARY  = new Color(100, 116, 139); // Slate-500
    public static final Color TEXT_MUTED      = new Color(148, 163, 184); // Slate-400
    public static final Color BORDER_COLOR    = new Color(226, 232, 240); // Slate-200
    public static final Color ROW_ALT         = new Color(248, 250, 252); // Slate-50
    public static final Color INPUT_FOCUS     = new Color(147, 197, 253); // Blue-300
    public static final Color HEADER_BG       = Color.WHITE;

    // Gradient pairs for KPI cards
    public static final Color[] GRAD_BLUE   = {new Color(59,  130, 246), new Color(37,  99, 235)};
    public static final Color[] GRAD_GREEN  = {new Color(34,  197,  94), new Color(22, 163,  74)};
    public static final Color[] GRAD_VIOLET = {new Color(168,  85, 247), new Color(139, 92, 246)};
    public static final Color[] GRAD_ORANGE = {new Color(251, 146,  60), new Color(234,  88,  12)};
    public static final Color[] GRAD_TEAL   = {new Color(20,  184, 166), new Color(13,  148, 136)};
    public static final Color[] GRAD_INDIGO = {new Color(99,  102, 241), new Color(79,   70, 229)};

    // =========================================================================
    // TYPOGRAPHY
    // =========================================================================
    public static final Font HEADER_FONT     = new Font("Segoe UI", Font.BOLD,  24);
    public static final Font SUBHEADER_FONT  = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font SECTION_FONT    = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font NORMAL_FONT     = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL_FONT      = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font BUTTON_FONT     = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font MONO_FONT       = new Font("Consolas",  Font.PLAIN, 12);
    public static final Font BADGE_FONT      = new Font("Segoe UI", Font.BOLD,  11);
    public static final Font CAPTION_FONT    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font TITLE_FONT      = new Font("Segoe UI", Font.BOLD,  28);

    // =========================================================================
    // ROUNDED BORDER
    // =========================================================================

    /**
     * A border that draws a rounded rectangle, used for cards, inputs, and dialogs.
     */
    public static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final int thickness;

        public RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x + thickness / 2f, y + thickness / 2f,
                    width - thickness, height - thickness, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int ins = radius / 2 + thickness;
            return new Insets(ins, ins, ins, ins);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            int ins = radius / 2 + thickness;
            insets.set(ins, ins, ins, ins);
            return insets;
        }
    }

    // =========================================================================
    // BUTTONS
    // =========================================================================

    public static JButton createPrimaryButton(String text) {
        return styledRoundedButton(text, PRIMARY_COLOR, PRIMARY_DARK, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return styledRoundedButton(text, SUCCESS_COLOR, SUCCESS_DARK, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return styledRoundedButton(text, DANGER_COLOR, DANGER_DARK, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return styledRoundedButton(text, SECONDARY_COLOR, SECONDARY_COLOR.darker(), Color.WHITE);
    }

    public static JButton createWarningButton(String text) {
        return styledRoundedButton(text, WARNING_COLOR, WARNING_COLOR.darker(), Color.WHITE);
    }

    public static JButton createGhostButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(BORDER_COLOR);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(BUTTON_FONT);
        btn.setForeground(TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerGhostButton(String text) {
        JButton btn = createGhostButton(text);
        btn.setForeground(DANGER_COLOR);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setForeground(DANGER_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setForeground(DANGER_COLOR); }
        });
        return btn;
    }

    private static JButton styledRoundedButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text) {
            private Color currentBg = bg;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hoverBg : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(BUTTON_FONT);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Sidebar / menu-style button (dark background, light text, left-aligned).
     */
    public static JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(new Color(203, 213, 225)); // Slate-300
        btn.setBackground(SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setMaximumSize(new Dimension(280, 46));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!btn.getBackground().equals(PRIMARY_DARK)) btn.setBackground(SIDEBAR_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!btn.getBackground().equals(PRIMARY_DARK)) btn.setBackground(SIDEBAR_BG);
            }
        });
        return btn;
    }

    // =========================================================================
    // TOOLBAR
    // =========================================================================

    /**
     * Creates a styled action toolbar panel (white background, subtle bottom border).
     * Add buttons directly to this panel using its FlowLayout.
     */
    public static JPanel createToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(4, 8, 4, 8)
        ));
        return bar;
    }

    public static JPanel createToolbarRight() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(4, 8, 4, 8)
        ));
        return bar;
    }

    // =========================================================================
    // CARDS
    // =========================================================================

    /**
     * Standard white card with subtle rounded border.
     */
=======
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UIHelper {
    // Modern Color Palette
    public static final Color PRIMARY_COLOR = new Color(59, 130, 246); // Blue
    public static final Color SECONDARY_COLOR = new Color(99, 102, 241); // Indigo
    public static final Color SUCCESS_COLOR = new Color(34, 197, 94); // Green
    public static final Color DANGER_COLOR = new Color(239, 68, 68); // Red
    public static final Color WARNING_COLOR = new Color(251, 146, 60); // Orange
    public static final Color INFO_COLOR = new Color(14, 165, 233); // Cyan
    
    public static final Color SIDEBAR_BG = new Color(30, 41, 59); // Slate 800
    public static final Color CONTENT_BG = new Color(248, 250, 252); // Slate 50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42); // Slate 900
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139); // Slate 500
    public static final Color BORDER_COLOR = new Color(226, 232, 240); // Slate 200
    
    public static Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static Font SUBHEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, PRIMARY_COLOR, Color.WHITE);
        return button;
    }
    
    public static JButton createSuccessButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, SUCCESS_COLOR, Color.WHITE);
        return button;
    }
    
    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, DANGER_COLOR, Color.WHITE);
        return button;
    }
    
    public static JButton createSecondaryButton(String text) {
        // Secondary buttons now use a filled style for consistency (indigo background, white text)
        JButton button = new JButton(text);
        styleButton(button, SECONDARY_COLOR, Color.WHITE);
        return button;
    }
    
    private static void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setFont(BUTTON_FONT);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }
    
>>>>>>> 0bc87d04903327a398d57bc0ad7a11b23bfb99e6
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
<<<<<<< HEAD
            new RoundedBorder(BORDER_COLOR, 12, 1),
=======
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
>>>>>>> 0bc87d04903327a398d57bc0ad7a11b23bfb99e6
            new EmptyBorder(20, 20, 20, 20)
        ));
        return card;
    }
<<<<<<< HEAD

    /**
     * Compact card with less padding, useful for smaller info blocks.
     */
    public static JPanel createCompactCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 10, 1),
            new EmptyBorder(12, 16, 12, 16)
        ));
        return card;
    }

    /**
     * Gradient KPI card — paints a top-to-bottom color gradient background.
     */
    public static JPanel createGradientCard(Color fromColor, Color toColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, fromColor, 0, getHeight(), toColor);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        return card;
    }

    // =========================================================================
    // LABELS
    // =========================================================================

=======
    
    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(NORMAL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }
    
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(NORMAL_FONT);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }
    
>>>>>>> 0bc87d04903327a398d57bc0ad7a11b23bfb99e6
    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADER_FONT);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }
<<<<<<< HEAD

=======
    
>>>>>>> 0bc87d04903327a398d57bc0ad7a11b23bfb99e6
    public static JLabel createSubHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SUBHEADER_FONT);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

<<<<<<< HEAD
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(NORMAL_FONT);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel createSecondaryLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SMALL_FONT);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    public static JLabel createCaptionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(CAPTION_FONT);
        label.setForeground(TEXT_MUTED);
        return label;
    }

    /**
     * Page section header with title + subtitle.
     */
    public static JPanel createPageHeader(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CONTENT_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(HEADER_FONT);
        titleLbl.setForeground(TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(SMALL_FONT);
        subLbl.setForeground(TEXT_SECONDARY);

        panel.add(titleLbl);
        panel.add(Box.createRigidArea(new Dimension(0, 2)));
        panel.add(subLbl);
        return panel;
    }

    /**
     * Status badge — colored pill label for PENDING, COMPLETED, CANCELLED, etc.
     */
    public static JLabel createStatusBadge(String status) {
        JLabel badge = new JLabel(status.toUpperCase(), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(BADGE_FONT);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));

        switch (status.toUpperCase()) {
            case "COMPLETED": case "IN STOCK": case "ACTIVE":
                badge.setBackground(SUCCESS_LIGHT);
                badge.setForeground(new Color(21, 128, 61));
                break;
            case "PENDING": case "UNDER REVIEW":
                badge.setBackground(WARNING_LIGHT);
                badge.setForeground(new Color(154, 52, 18));
                break;
            case "CANCELLED": case "OUT OF STOCK": case "LOCKED": case "REPORTED":
                badge.setBackground(DANGER_LIGHT);
                badge.setForeground(new Color(185, 28, 28));
                break;
            case "LOW STOCK":
                badge.setBackground(WARNING_LIGHT);
                badge.setForeground(new Color(146, 64, 14));
                break;
            default:
                badge.setBackground(new Color(241, 245, 249));
                badge.setForeground(TEXT_SECONDARY);
        }
        return badge;
    }

    /**
     * Rank badge label — colored pill showing 1st / 2nd / 3rd rank.
     */
    public static JLabel createRankBadge(int rank) {
        String text;
        Color bg;
        switch (rank) {
            case 1:  text = "#1";  bg = new Color(234, 179,   8); break;
            case 2:  text = "#2";  bg = new Color(156, 163, 175); break;
            case 3:  text = "#3";  bg = new Color(180,  83,   9); break;
            default: text = "#" + rank; bg = BORDER_COLOR; break;
        }
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(BADGE_FONT);
        badge.setForeground(rank <= 3 ? Color.WHITE : TEXT_PRIMARY);
        badge.setBackground(bg);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        badge.setPreferredSize(new Dimension(36, 22));
        return badge;
    }

    /**
     * SQL-badge label shown on dashboard analytics cards.
     */
    public static JLabel createSqlBadge(String clauseText) {
        JLabel badge = new JLabel(clauseText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Consolas", Font.BOLD, 10));
        badge.setForeground(new Color(148, 203, 255));
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }

    /**
     * Section divider — thin line with uppercase label.
     */
    public static JPanel createSectionDivider(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(8, 0, 8, 0));

        JLabel label = new JLabel(title.toUpperCase());
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(TEXT_SECONDARY);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);

        panel.add(label, BorderLayout.WEST);
        panel.add(sep,   BorderLayout.CENTER);
        return panel;
    }

    /**
     * SQL query preview area — dark code background.
     */
    public static JTextArea createQueryPreviewArea(String sql) {
        JTextArea area = new JTextArea(sql);
        area.setFont(MONO_FONT);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(false);
        area.setBackground(new Color(30, 41, 59));
        area.setForeground(new Color(148, 203, 255));
        area.setCaretColor(Color.WHITE);
        area.setBorder(new EmptyBorder(10, 12, 10, 12));
        return area;
    }

    /**
     * Empty state panel — shown when a table/list has no data.
     */
    public static JPanel createEmptyState(String iconText, String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BG);
        panel.setBorder(new EmptyBorder(40, 20, 40, 20));

        JLabel icon = new JLabel(iconText, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(SUBHEADER_FONT);
        titleLbl.setForeground(TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel(subtitle, SwingConstants.CENTER);
        subLbl.setFont(SMALL_FONT);
        subLbl.setForeground(TEXT_SECONDARY);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(icon);
        panel.add(Box.createRigidArea(new Dimension(0, 12)));
        panel.add(titleLbl);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        panel.add(subLbl);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        return panel;
    }

    // =========================================================================
    // INPUT FIELDS
    // =========================================================================

    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(NORMAL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 8, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        addFocusHighlight(field);
        return field;
    }

    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(NORMAL_FONT);
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left + 2, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                }
            }
        };
        field.setFont(NORMAL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 8, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        addFocusHighlight(field);
        return field;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(NORMAL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 8, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        addFocusHighlight(field);
        return field;
    }

    public static JComboBox<?> styleComboBox(JComboBox<?> combo) {
        combo.setFont(NORMAL_FONT);
        combo.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 8, 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        return combo;
    }

    private static void addFocusHighlight(JComponent field) {
        field.addFocusListener(new FocusAdapter() {
            Border normalBorder = field.getBorder();
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(INPUT_FOCUS, 8, 2),
                    new EmptyBorder(7, 11, 7, 11)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(BORDER_COLOR, 8, 1),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }

    // =========================================================================
    // TABLES
    // =========================================================================

    /**
     * Applies the standard table style.
     */
=======
    /**
     * Create a button styled like the sidebar/menu buttons in MainFrame
     */
    public static JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        // Match MainFrame sidebar/menu button style: light foreground on dark bg, flat appearance
        btn.setForeground(new Color(203, 213, 225)); // Slate 300
        btn.setBackground(SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 25, 12, 25));
        btn.setMaximumSize(new Dimension(250, 45));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(PRIMARY_COLOR)) {
                    btn.setBackground(new Color(51, 65, 85)); // Slate 700
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!btn.getBackground().equals(PRIMARY_COLOR)) {
                    btn.setBackground(SIDEBAR_BG);
                }
            }
        });

        return btn;
    }
    
>>>>>>> 0bc87d04903327a398d57bc0ad7a11b23bfb99e6
    public static void styleTable(JTable table) {
        table.setFont(NORMAL_FONT);
        table.setRowHeight(40);
        table.setGridColor(BORDER_COLOR);
<<<<<<< HEAD
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR)
        );
        table.setFillsViewportHeight(true);
    }

    /**
     * Applies alternating row background colors.
     */
    public static void applyAlternatingRows(JTable table) {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                }
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return this;
            }
        });
    }

    /**
     * Applies a status badge renderer to a specific column.
     * Values are rendered as colored pill badges.
     */
    public static void applyStatusRenderer(JTable table, int column) {
        table.getColumnModel().getColumn(column).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
                wrapper.setBackground(isSelected ? PRIMARY_LIGHT : (row % 2 == 0 ? Color.WHITE : ROW_ALT));
                if (value != null) {
                    wrapper.add(createStatusBadge(value.toString()));
                }
                return wrapper;
            }
        });
    }

    // =========================================================================
    // DIALOGS
    // =========================================================================

    public static void styleDialog(JDialog dialog) {
        dialog.getContentPane().setBackground(CONTENT_BG);
        dialog.getRootPane().setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    /**
     * Creates a styled dialog header panel with a title and subtitle.
     */
    public static JPanel createDialogHeader(String title, String subtitle, Color accentColor) {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentColor);
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(SUBHEADER_FONT);
        titleLbl.setForeground(TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(SMALL_FONT);
        subLbl.setForeground(TEXT_SECONDARY);

        header.add(titleLbl);
        header.add(Box.createRigidArea(new Dimension(0, 2)));
        header.add(subLbl);
        return header;
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // =========================================================================
    // GLOBAL UI MANAGER SETUP
    // =========================================================================

    /**
     * Applies custom UIManager overrides for a more modern appearance.
     * Call this from Main.java before creating any frames.
     */
    public static void applyGlobalStyles() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Table header
        UIManager.put("TableHeader.background",   new Color(248, 250, 252));
        UIManager.put("TableHeader.foreground",   TEXT_SECONDARY);
        UIManager.put("Table.background",         Color.WHITE);
        UIManager.put("Table.alternateRowColor",  ROW_ALT);
        UIManager.put("Table.selectionBackground", PRIMARY_LIGHT);
        UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
        UIManager.put("Table.gridColor",          BORDER_COLOR);

        // Scrollbars
        UIManager.put("ScrollBar.width", 8);
        UIManager.put("ScrollBar.thumb", new Color(203, 213, 225));
        UIManager.put("ScrollBar.track", new Color(241, 245, 249));

        // Panels & components
        UIManager.put("Panel.background",         CONTENT_BG);
        UIManager.put("OptionPane.background",    Color.WHITE);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);

        // TabbedPane
        UIManager.put("TabbedPane.selected",         Color.WHITE);
        UIManager.put("TabbedPane.background",        CONTENT_BG);
        UIManager.put("TabbedPane.tabAreaBackground", CONTENT_BG);
        UIManager.put("TabbedPane.selectedForeground", PRIMARY_COLOR);
        UIManager.put("TabbedPane.foreground",        TEXT_SECONDARY);
        UIManager.put("TabbedPane.font",              SECTION_FONT);

        // ComboBox
        UIManager.put("ComboBox.background",      Color.WHITE);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_LIGHT);
        UIManager.put("ComboBox.font",            NORMAL_FONT);

        // Spinner
        UIManager.put("Spinner.background",       Color.WHITE);
        UIManager.put("Spinner.font",             NORMAL_FONT);

        // Tooltip
        UIManager.put("ToolTip.background",       new Color(30, 41, 59));
        UIManager.put("ToolTip.foreground",       Color.WHITE);
        UIManager.put("ToolTip.font",             SMALL_FONT);
        UIManager.put("ToolTip.border",           BorderFactory.createEmptyBorder(6, 10, 6, 10));

        // SplitPane
        UIManager.put("SplitPane.dividerSize",    6);
        UIManager.put("SplitPaneDivider.draggingColor", BORDER_COLOR);
    }
=======
        table.setSelectionBackground(new Color(219, 234, 254)); // Blue 100
        table.setSelectionForeground(TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(CONTENT_BG);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
    }
    
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static boolean showConfirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm", 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
>>>>>>> 0bc87d04903327a398d57bc0ad7a11b23bfb99e6
}