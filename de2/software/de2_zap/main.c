#include <stdio.h>
#include <unistd.h>
#include <system.h>
#include <io.h>
#include <time.h>
#include <assert.h>
#include <sys/time.h>
#include "Player.h"

#include "Field.h"
#include "Draw.h"
#include "Card.h"
#include "Communication.h"
#include "sys/alt_timestamp.h"

#define switches (volatile char *) 0x0004430
#define leds (char *) 0x0004420

#define FPS 2.0
#define TICKS_PER_FRAME (1000.0 / FPS)
#define PS 256

int main() {
    printf("hey!\n");
    alt_timestamp_start();
    srand(alt_timestamp());
    CardCtrl *cardCtrl = (CardCtrl*)malloc(sizeof(CardCtrl));
    PlayerCtrl* playerCtrl = (PlayerCtrl*)malloc(sizeof(PlayerCtrl));
    initCards(cardCtrl);
    initPlayers(playerCtrl, 7);
    int i, j;
    for (i = 0; i < NUM_PLAYERS; i++) {
        for (j = 0; j < 3; j++) {
            playerCtrl->players[i].blueCards[j] = testDrawCard(cardCtrl);
            printf("The drawn card is %d\n", playerCtrl->players[i].blueCards[j]);
        }
    }

    field = malloc(sizeof(Field));
    alt_up_char_buffer_dev *charBuffer = initCharBuffer();
    alt_up_pixel_buffer_dma_dev *pixelBuffer = initPixelBuffer();
    initField(field, playerCtrl, cardCtrl, charBuffer);

    while (1) {
    	*leds = *switches;
    	alt_timestamp_start();
    	alt_up_char_buffer_clear(charBuffer);
    	runField(field);
    	srand(alt_timestamp());
    }

    while (1){
        int listening = 1;
        while (listening) {
            Message message = receivedFromAndroid();
            switch (message.type) {
            case DRAW_CARDS:
                drawCardsForId(message.id, cardCtrl, message.count);
                break;
            case UPDATE_HAND:
                updateHandForId(playerCtrl, message.id, message.cards);
                break;
            case UPDATE_BLUE:
                updateBlueCardsForId(playerCtrl, message.id, message.cards);
                break;
            case UPDATE_LIVES:
                updateLivesForId(playerCtrl, message.id, message.count);
                break;
            case GATLING:
                startGatling(playerCtrl, message.id);
                break;
            case ALIENS:
                startAliens(playerCtrl, message.id);
                break;
            case BEER:
                updateLivesForId(playerCtrl, message.id, message.count);
                break;
            case GENERAL_STORE:
                startStore(playerCtrl, message.id,cardCtrl);
                break;
            case SALOON:
                startSaloon(playerCtrl);
                break;
            case ZAP:
                startZap(playerCtrl, message.id);
                break;
            case PANIC:
                startPanic(playerCtrl, message.id, playerCtrl->turn);
                break;
            case CAT_BALOU:
                startCatBalou(playerCtrl, message.id);
                break;
            case DUEL:
                startDuel(playerCtrl, message.id, playerCtrl->turn);
                break;
            case JAIL:
                sendJail(message.id);
                break;
            case END_TURN:
                listening = 0;
            default:
                break;
            }
        }
        endTurn(playerCtrl);
    }
    return 0;
}