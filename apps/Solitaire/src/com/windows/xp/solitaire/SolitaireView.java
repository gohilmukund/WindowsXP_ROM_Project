package com.windows.xp.solitaire;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SolitaireView extends View {

    public interface OnSolitaireEventListener {
        void onScoreChanged(int score);
        void onGameWon();
    }

    public enum Suit {
        SPADES("♠", Color.BLACK),
        HEARTS("♥", 0xFFD80000),
        DIAMONDS("♦", 0xFFD80000),
        CLUBS("♣", Color.BLACK);

        public final String symbol;
        public final int color;

        Suit(String symbol, int color) {
            this.symbol = symbol;
            this.color = color;
        }

        public boolean isRed() {
            return this == HEARTS || this == DIAMONDS;
        }
    }

    public static class Card {
        public final int rank; // 1 = A, 11 = J, 12 = Q, 13 = K
        public final Suit suit;
        public boolean isFaceUp = false;

        public Card(int rank, Suit suit) {
            this.rank = rank;
            this.suit = suit;
        }

        public String getRankString() {
            switch (rank) {
                case 1: return "A";
                case 11: return "J";
                case 12: return "Q";
                case 13: return "K";
                default: return String.valueOf(rank);
            }
        }
    }

    private List<Card> stock = new ArrayList<>();
    private List<Card> waste = new ArrayList<>();
    private List<List<Card>> foundations = new ArrayList<>(); // 4 piles
    private List<List<Card>> tableau = new ArrayList<>();    // 7 columns

    // Dragging state
    private List<Card> draggedCards = new ArrayList<>();
    private int dragSourceType = -1; // 0 = waste, 1 = tableau
    private int dragSourceIndex = -1;
    private float dragX = 0f;
    private float dragY = 0f;
    private float dragTouchOffsetX = 0f;
    private float dragTouchOffsetY = 0f;

    private int score = 0;
    private boolean isGameWon = false;
    private OnSolitaireEventListener listener;

    // Layout metrics
    private float cardW = 70f;
    private float cardH = 98f;
    private float cardCorner = 6f;
    private float marginX = 16f;
    private float marginY = 16f;
    private float colGap = 16f;
    private float cardOffsetY = 24f;

    private Paint cardBgPaint;
    private Paint cardBorderPaint;
    private Paint cardBackPaint;
    private Paint cardBackInnerPaint;
    private Paint textRedPaint;
    private Paint textBlackPaint;
    private Paint suitBigPaint;
    private Paint placeholderPaint;

    public SolitaireView(Context context) {
        super(context);
        init();
    }

    public SolitaireView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        cardBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBgPaint.setColor(Color.WHITE);

        cardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBorderPaint.setColor(0xFF222222);
        cardBorderPaint.setStyle(Paint.Style.STROKE);
        cardBorderPaint.setStrokeWidth(1.5f);

        cardBackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBackPaint.setColor(0xFF0038A8);

        cardBackInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBackInnerPaint.setColor(0xFF1E60D0);
        cardBackInnerPaint.setStyle(Paint.Style.STROKE);
        cardBackInnerPaint.setStrokeWidth(3f);

        textRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textRedPaint.setColor(0xFFD80000);
        textRedPaint.setFakeBoldText(true);

        textBlackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textBlackPaint.setColor(Color.BLACK);
        textBlackPaint.setFakeBoldText(true);

        suitBigPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        suitBigPaint.setTextAlign(Paint.Align.CENTER);

        placeholderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        placeholderPaint.setColor(0x44FFFFFF);
        placeholderPaint.setStyle(Paint.Style.STROKE);
        placeholderPaint.setStrokeWidth(2f);

        for (int i = 0; i < 4; i++) foundations.add(new ArrayList<Card>());
        for (int i = 0; i < 7; i++) tableau.add(new ArrayList<Card>());

        startNewDeal();
    }

    public void setOnSolitaireEventListener(OnSolitaireEventListener listener) {
        this.listener = listener;
        if (listener != null) listener.onScoreChanged(score);
    }

    public void startNewDeal() {
        stock.clear();
        waste.clear();
        for (List<Card> f : foundations) f.clear();
        for (List<Card> t : tableau) t.clear();

        draggedCards.clear();
        score = 0;
        isGameWon = false;

        List<Card> deck = new ArrayList<>();
        for (Suit s : Suit.values()) {
            for (int r = 1; r <= 13; r++) {
                deck.add(new Card(r, s));
            }
        }
        Collections.shuffle(deck);

        // Deal 7 tableau columns
        int deckIdx = 0;
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j <= i; j++) {
                Card c = deck.get(deckIdx++);
                c.isFaceUp = (j == i);
                tableau.get(i).add(c);
            }
        }

        // Remaining 24 cards to stock
        while (deckIdx < deck.size()) {
            Card c = deck.get(deckIdx++);
            c.isFaceUp = false;
            stock.add(c);
        }

        if (listener != null) listener.onScoreChanged(score);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        colGap = (w - (2 * marginX) - (7 * cardW)) / 6f;
        if (colGap < 8f) {
            cardW = (w - (2 * marginX) - (6 * 10f)) / 7f;
            cardH = cardW * 1.4f;
            colGap = 10f;
        }
        cardOffsetY = cardH * 0.26f;
        textRedPaint.setTextSize(cardW * 0.22f);
        textBlackPaint.setTextSize(cardW * 0.22f);
        suitBigPaint.setTextSize(cardW * 0.45f);
    }

    private RectF getStockRect() {
        return new RectF(marginX, marginY, marginX + cardW, marginY + cardH);
    }

    private RectF getWasteRect() {
        float left = marginX + cardW + colGap;
        return new RectF(left, marginY, left + cardW, marginY + cardH);
    }

    private RectF getFoundationRect(int idx) {
        float left = marginX + (3 + idx) * (cardW + colGap);
        return new RectF(left, marginY, left + cardW, marginY + cardH);
    }

    private float getTableauColLeft(int col) {
        return marginX + col * (cardW + colGap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return handleTouchDown(x, y);
            case MotionEvent.ACTION_MOVE:
                if (!draggedCards.isEmpty()) {
                    dragX = x - dragTouchOffsetX;
                    dragY = y - dragTouchOffsetY;
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!draggedCards.isEmpty()) {
                    handleDrop(x, y);
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private boolean handleTouchDown(float x, float y) {
        if (isGameWon) return false;

        // 1. Stock pile tapped
        if (getStockRect().contains(x, y)) {
            if (!stock.isEmpty()) {
                Card c = stock.remove(stock.size() - 1);
                c.isFaceUp = true;
                waste.add(c);
            } else {
                // Recycle waste to stock
                while (!waste.isEmpty()) {
                    Card c = waste.remove(waste.size() - 1);
                    c.isFaceUp = false;
                    stock.add(c);
                }
            }
            invalidate();
            return true;
        }

        // 2. Waste pile tapped/dragged
        if (!waste.isEmpty() && getWasteRect().contains(x, y)) {
            Card topWaste = waste.get(waste.size() - 1);
            if (tryAutoMoveCard(topWaste, true)) {
                waste.remove(waste.size() - 1);
                invalidate();
                return true;
            }
            // Begin dragging waste card
            draggedCards.clear();
            draggedCards.add(waste.remove(waste.size() - 1));
            dragSourceType = 0;
            dragTouchOffsetX = x - getWasteRect().left;
            dragTouchOffsetY = y - getWasteRect().top;
            dragX = getWasteRect().left;
            dragY = getWasteRect().top;
            invalidate();
            return true;
        }

        // 3. Tableau columns
        float tableauTop = marginY + cardH + 16f;
        for (int col = 0; col < 7; col++) {
            float left = getTableauColLeft(col);
            List<Card> pile = tableau.get(col);
            if (x >= left && x <= left + cardW) {
                if (pile.isEmpty() && y >= tableauTop && y <= tableauTop + cardH) {
                    return true;
                }
                for (int i = pile.size() - 1; i >= 0; i--) {
                    float cy = tableauTop + (i * cardOffsetY);
                    RectF cardRect = new RectF(left, cy, left + cardW, cy + cardH);
                    if (cardRect.contains(x, y)) {
                        Card clickedCard = pile.get(i);
                        if (!clickedCard.isFaceUp) {
                            if (i == pile.size() - 1) {
                                clickedCard.isFaceUp = true;
                                invalidate();
                                return true;
                            }
                            return false;
                        }

                        // Top card tap: attempt auto-move
                        if (i == pile.size() - 1 && tryAutoMoveCard(clickedCard, false)) {
                            pile.remove(pile.size() - 1);
                            if (!pile.isEmpty()) pile.get(pile.size() - 1).isFaceUp = true;
                            invalidate();
                            return true;
                        }

                        // Drag sub-stack
                        draggedCards.clear();
                        for (int k = i; k < pile.size(); k++) {
                            draggedCards.add(pile.get(k));
                        }
                        for (int k = pile.size() - 1; k >= i; k--) {
                            pile.remove(k);
                        }
                        dragSourceType = 1;
                        dragSourceIndex = col;
                        dragTouchOffsetX = x - left;
                        dragTouchOffsetY = y - cy;
                        dragX = left;
                        dragY = cy;
                        invalidate();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean tryAutoMoveCard(Card card, boolean fromWaste) {
        // Try foundation first
        for (int f = 0; f < 4; f++) {
            List<Card> fp = foundations.get(f);
            if (canPlaceOnFoundation(card, fp)) {
                fp.add(card);
                score += 10;
                if (listener != null) listener.onScoreChanged(score);
                checkWinCondition();
                return true;
            }
        }
        // Try tableau
        for (int t = 0; t < 7; t++) {
            List<Card> tp = tableau.get(t);
            if (canPlaceOnTableau(card, tp)) {
                tp.add(card);
                score += fromWaste ? 5 : 0;
                if (listener != null) listener.onScoreChanged(score);
                return true;
            }
        }
        return false;
    }

    private void handleDrop(float dropX, float dropY) {
        Card rootCard = draggedCards.get(0);
        boolean placed = false;

        // Check foundation if single card
        if (draggedCards.size() == 1) {
            for (int f = 0; f < 4; f++) {
                if (getFoundationRect(f).contains(dropX, dropY)) {
                    List<Card> fp = foundations.get(f);
                    if (canPlaceOnFoundation(rootCard, fp)) {
                        fp.add(rootCard);
                        placed = true;
                        score += 10;
                        break;
                    }
                }
            }
        }

        // Check tableau
        if (!placed) {
            float tableauTop = marginY + cardH + 16f;
            for (int t = 0; t < 7; t++) {
                float left = getTableauColLeft(t);
                List<Card> tp = tableau.get(t);
                float topY = tp.isEmpty() ? tableauTop : tableauTop + ((tp.size() - 1) * cardOffsetY);
                RectF targetRect = new RectF(left - 10f, topY - 10f, left + cardW + 10f, topY + cardH + 30f);

                if (targetRect.contains(dropX, dropY)) {
                    if (canPlaceOnTableau(rootCard, tp)) {
                        tp.addAll(draggedCards);
                        placed = true;
                        score += (dragSourceType == 0) ? 5 : 0;
                        break;
                    }
                }
            }
        }

        // Restore to source if drop failed
        if (!placed) {
            if (dragSourceType == 0) {
                waste.addAll(draggedCards);
            } else if (dragSourceType == 1) {
                tableau.get(dragSourceIndex).addAll(draggedCards);
            }
        } else {
            // Flip exposed card in source tableau if needed
            if (dragSourceType == 1) {
                List<Card> src = tableau.get(dragSourceIndex);
                if (!src.isEmpty()) {
                    src.get(src.size() - 1).isFaceUp = true;
                }
            }
            if (listener != null) listener.onScoreChanged(score);
            checkWinCondition();
        }

        draggedCards.clear();
        invalidate();
    }

    private boolean canPlaceOnFoundation(Card card, List<Card> foundationPile) {
        if (foundationPile.isEmpty()) {
            return card.rank == 1; // Ace
        }
        Card top = foundationPile.get(foundationPile.size() - 1);
        return card.suit == top.suit && card.rank == top.rank + 1;
    }

    private boolean canPlaceOnTableau(Card card, List<Card> tableauPile) {
        if (tableauPile.isEmpty()) {
            return card.rank == 13; // King only on empty column
        }
        Card top = tableauPile.get(tableauPile.size() - 1);
        return top.isFaceUp && (card.suit.isRed() != top.suit.isRed()) && (card.rank == top.rank - 1);
    }

    private void checkWinCondition() {
        int foundationCount = 0;
        for (List<Card> f : foundations) {
            foundationCount += f.size();
        }
        if (foundationCount == 52) {
            isGameWon = true;
            if (listener != null) listener.onGameWon();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. Draw Stock
        RectF stockRect = getStockRect();
        if (stock.isEmpty()) {
            canvas.drawRoundRect(stockRect, cardCorner, cardCorner, placeholderPaint);
        } else {
            drawCardBack(canvas, stockRect);
        }

        // 2. Draw Waste
        RectF wasteRect = getWasteRect();
        if (waste.isEmpty()) {
            canvas.drawRoundRect(wasteRect, cardCorner, cardCorner, placeholderPaint);
        } else {
            drawCardFront(canvas, wasteRect, waste.get(waste.size() - 1));
        }

        // 3. Draw Foundations
        for (int f = 0; f < 4; f++) {
            RectF fRect = getFoundationRect(f);
            List<Card> fp = foundations.get(f);
            if (fp.isEmpty()) {
                canvas.drawRoundRect(fRect, cardCorner, cardCorner, placeholderPaint);
            } else {
                drawCardFront(canvas, fRect, fp.get(fp.size() - 1));
            }
        }

        // 4. Draw Tableau
        float tableauTop = marginY + cardH + 16f;
        for (int col = 0; col < 7; col++) {
            float left = getTableauColLeft(col);
            List<Card> pile = tableau.get(col);
            if (pile.isEmpty()) {
                canvas.drawRoundRect(new RectF(left, tableauTop, left + cardW, tableauTop + cardH), cardCorner, cardCorner, placeholderPaint);
            } else {
                for (int i = 0; i < pile.size(); i++) {
                    float cy = tableauTop + (i * cardOffsetY);
                    RectF cRect = new RectF(left, cy, left + cardW, cy + cardH);
                    Card c = pile.get(i);
                    if (c.isFaceUp) {
                        drawCardFront(canvas, cRect, c);
                    } else {
                        drawCardBack(canvas, cRect);
                    }
                }
            }
        }

        // 5. Draw Dragged Stack
        if (!draggedCards.isEmpty()) {
            for (int i = 0; i < draggedCards.size(); i++) {
                float cy = dragY + (i * cardOffsetY);
                RectF dRect = new RectF(dragX, cy, dragX + cardW, cy + cardH);
                drawCardFront(canvas, dRect, draggedCards.get(i));
            }
        }
    }

    private void drawCardBack(Canvas canvas, RectF rect) {
        canvas.drawRoundRect(rect, cardCorner, cardCorner, cardBgPaint);
        RectF inner = new RectF(rect.left + 3f, rect.top + 3f, rect.right - 3f, rect.bottom - 3f);
        canvas.drawRoundRect(inner, cardCorner - 1, cardCorner - 1, cardBackPaint);
        canvas.drawRoundRect(inner, cardCorner - 1, cardCorner - 1, cardBackInnerPaint);
        canvas.drawRoundRect(rect, cardCorner, cardCorner, cardBorderPaint);
    }

    private void drawCardFront(Canvas canvas, RectF rect, Card card) {
        canvas.drawRoundRect(rect, cardCorner, cardCorner, cardBgPaint);
        canvas.drawRoundRect(rect, cardCorner, cardCorner, cardBorderPaint);

        Paint tp = card.suit.isRed() ? textRedPaint : textBlackPaint;

        // Top-left rank & suit
        String rankStr = card.getRankString();
        canvas.drawText(rankStr, rect.left + 5f, rect.top + (tp.getTextSize() * 0.95f), tp);
        canvas.drawText(card.suit.symbol, rect.left + 5f, rect.top + (tp.getTextSize() * 2f), tp);

        // Center big suit
        suitBigPaint.setColor(card.suit.color);
        canvas.drawText(card.suit.symbol, rect.centerX(), rect.centerY() + (suitBigPaint.getTextSize() * 0.35f), suitBigPaint);
    }
}
