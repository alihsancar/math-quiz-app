package com.example.mathquiz;

/**
 * MessageType - Socket üzerinden gönderilen mesaj tipleri
 */
public class MessageType {
    // Oyun başlangıç mesajları
    public static final String GAME_READY = "GAME_READY";       // Client hazır
    public static final String START_GAME = "START_GAME";       // Oyun başlat

    // Oyun akış mesajları
    public static final String QUESTION = "QUESTION";           // Yeni soru
    public static final String ANSWER = "ANSWER";               // Cevap gönderme
    public static final String ANSWER_RESULT = "ANSWER_RESULT"; // Cevap sonucu

    // Skor mesajları
    public static final String SCORE_UPDATE = "SCORE_UPDATE";   // Skor güncelleme

    // Tur mesajları
    public static final String TURN_START = "TURN_START";       // Tur başlangıcı
    public static final String TURN_END = "TURN_END";           // Tur sonu
    public static final String NEXT_TURN = "NEXT_TURN";         // Sonraki tura geç

    // Oyun sonu mesajları
    public static final String END_GAME = "END_GAME";           // Oyun bitti

    // Bağlantı mesajları
    public static final String PING = "PING";                   // Bağlantı kontrolü
    public static final String PONG = "PONG";                   // Ping cevabı
    public static final String DISCONNECT = "DISCONNECT";       // Bağlantı kesme
    public static final String PLAYER_LEFT = "PLAYER_LEFT";     // Oyuncu ayrıldı

    // Oyuncu bilgileri
    public static final String PLAYER_INFO = "PLAYER_INFO";     // Oyuncu bilgisi paylaşımı
    public static final String GAME_SETTINGS = "GAME_SETTINGS"; // Oyun ayarları
}