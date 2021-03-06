package com.zap.main;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.SystemClock;
import android.util.Log;

import com.zap.PlayerActivity;
import com.zap.PlayerHandCards;
import com.zap.PlayerStats;

// TODO Amitoj: Implement dying mechanics (3 cards for killing outlaw, etc)
// TODO Amitoj: Implement onEvent functions, test dynamite and jail

public class Player {
    public static Activity activity;
    public static PlayerActivity playerActivity = null;

    private String name;

    private CardController cc;
    private int lives;
    private int maxLives;
    private HashMap<Integer, Opponent> opponents;
    private String role;
    private int pid;
    private boolean turn;
    private boolean dead;
    private boolean zappedThisTurn;

    public String test_call;

    public static final String SHERIFF = "Sheriff";
    public static final String MUSTANG = "Mustang";
    public static final String JAIL = "Jail";
    private static final String SCOPE = "Scope";
    private static final String DUEL = "Duel";
    private static final String ALIENS = "Indians";
    private static final String GENERAL_STORE = "General Store";
    private static final String DYNAMITE = "Dynamite";

    public Player(String newName) {
        name = newName;
        cc = new CardController();
        opponents = new HashMap<Integer, Opponent>();
        lives = 4;
        maxLives = 4;
        turn = false;
        dead = false;
        zappedThisTurn = false;
        role = "N/A";
        test_call = "";
    }

    public Player() {
        this("name");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public HashMap getOpponents() {
        return this.opponents;
    }

    // TODO AMITOJ: test cases
    public int getRangeFromOpponent(int pid) {
        Opponent o = opponents.get(Integer.valueOf(pid));
        int range = o.getFixedRange();
        for (Card c : cc.getBlueCards()) {
            if (c.name.compareTo(MUSTANG) == 0) {
                range++;
            }
        }
        return range;
    }

    public boolean hasJail() {
        for (Card c : cc.getBlueCards()) {
            if (c.name.compareTo(JAIL) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasDynamite() {
        for (Card c : cc.getBlueCards()) {
            if (c.name.compareTo(DYNAMITE) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBarrel() {
        for (Card c : cc.getBlueCards()) {
            if (c.name.compareTo(JAIL) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMustang() {
        for (Card c : cc.getBlueCards()) {
            if (c.name.compareTo(MUSTANG) == 0) {
                return true;
            }
        }
        return false;
    }

    public void setLives(int lives) {
        if (!dead) {
            if (lives <= 0) {
                this.lives = 0;
                dead = true;
                cc.discardAll();
            } else if (lives > 0 && lives <= maxLives) {
                this.lives = lives;
            } else if (lives > maxLives) {
                this.lives = maxLives;
            }
            Comm.tellDE2UserUpdateLives(pid, this.lives);
        }
        activity.runOnUiThread(new Runnable() {
            public void run() {
                PlayerStats playerStats;
                if (playerActivity != null) {
                    playerStats = (PlayerStats) playerActivity.getSupportFragmentManager().findFragmentByTag(((PlayerActivity) activity).getTabStats());
                    playerStats.buildStats();
                }
            }
        });
    }

    public int getLives() {
        return lives;
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isTurn() {
        return turn;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPid() {
        return pid;
    }

    public int getNumberOfHandCards() {
        return cc.numberOfHandCards();
    }

    public int getNumberOfBlueCards() {
        return cc.numberOfBlueCards();
    }

    public ArrayList<Card> getHandCards() {
        return cc.getHandCards();
    }

    public ArrayList<Card> getBlueCards() {
        return cc.getBlueCards();
    }

    public void setRole(String role) {
        this.role = role;

        activity.runOnUiThread(new Runnable() {
            public void run() {
                playerActivity.getActionBar().setTitle("Role: " + getRole() + " " + String.valueOf(pid));
            }
        });

        if (role.compareTo(SHERIFF) == 0) {
            maxLives = 5;
            lives = 5;
        } else {
            maxLives = 4;
            lives = 4;
        }
    }

    public String getRole() {
        return role;
    }

    public void initOpponent(int pid, int range, String role) {
        if (opponents.get(Integer.valueOf(pid)) == null) {
            opponents.put(Integer.valueOf(pid), new Opponent(pid, range, role));
        }
    }

    public void setOpponentRole(int pid, String role) {
        Opponent o = opponents.get(Integer.valueOf(pid));
        if (o != null) {
            o.setRole(role);
        }
    }

    public void setOpponentRange(int pid, int range) {
        Opponent o = opponents.get(Integer.valueOf(pid));
        if (o != null) {
            o.setRange(range);
        }
    }

    public void setOpponentLives(int pid, int lives) {
        Log.v("TONY", "pid: " + pid + " lives " + lives);
        Opponent o = opponents.get(Integer.valueOf(pid));
        o.setLives(lives);
    }

    // TEST
    public void setOpponentBlueCards(int pid, ArrayList<Integer> cids) {
        Opponent o = opponents.get(Integer.valueOf(pid));
        o.discardAll();
        for (Integer i : cids) {
            o.playBlueCard(i.intValue());
        }
    }

    public void opponentPlayBlueCard(int pid, int cid) {
        Opponent o = opponents.get(Integer.valueOf(pid));
        o.playBlueCard(cid);
    }

    public void opponentDiscardBlueCard(int pid, int cid) {
        Opponent o = opponents.get(Integer.valueOf(pid));
        o.discardBlueCard(cid);
    }

    // Call this function to give player a card. For example, when he draws a
    // card
    public void receiveCard(int cid) {
        cc.receiveCard(cid);
    }

    // TODO AMITOJ: add unit tests
    public void receiveBlueCard(int cid) {
        cc.receiveBlueCard(cid);
    }

    public void discardCard(int cid) {
        cc.discardCard(cid);
        activity.runOnUiThread(new Runnable() {
            public void run() {
                PlayerHandCards playerHandCards;
                if (playerActivity != null) {
                    playerHandCards = (PlayerHandCards) playerActivity.getSupportFragmentManager().findFragmentByTag(((PlayerActivity) activity).getTabHandCards());
                    playerHandCards.buildCards();
                }
            }
        });
    }

    public void startTurn() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                PlayerStats playerStats;
                if (playerActivity != null) {
                    playerStats = (PlayerStats) playerActivity.getSupportFragmentManager().findFragmentByTag(((PlayerActivity) activity).getTabStats());
                    playerStats.buildStats();
                }
            }
        });

        turn = true;
        zappedThisTurn = false;

        // Draw for jail, if in jail
        for (Card c : cc.getBlueCards()) {
            if (c.name.compareTo(JAIL) == 0) {
                cc.discardCard(c.cid);
                Card t = getHandCards().get(getHandCards().size() - 1);
                discardCard(t.cid);
                Comm.tellDE2CardsInHand(pid, getNumberOfHandCards(), getHandCards());
                SystemClock.sleep(1500);
                Comm.tellDE2BlueCardsInFront(pid, getNumberOfBlueCards(), getBlueCards());
                if (t.suit != 'H') {
                    forceEndTurn();
                    return;
                }
                break;
            }
        }

        // Draw for dynamite, if there is dynamite
        for (Card c : cc.getBlueCards()) {
            if (c.name.compareTo(DYNAMITE) == 0) {
                Card t = getHandCards().get(getHandCards().size() - 1);
                discardCard(t.cid);
                Comm.tellDE2CardsInHand(pid, getNumberOfHandCards(), getHandCards());
                SystemClock.sleep(1500);
                Comm.tellDE2BlueCardsInFront(pid, getNumberOfBlueCards(), getBlueCards());
                if (t.suit == 'S' && t.number >= '2' && t.number <= '9') {
                    // TODO: take 3 hits here
                    cc.discardCard(c.cid);
                } else {
                    // TODO: pass dynamite to next player
                    cc.discardCard(c.cid);
                }
                break;
            }
        }
        drawCards(2);
    }

    public void endTurn() {
        if (cc.numberOfHandCards() > lives) {
            // TODO Tony: Disallow, force player to discard cards
            test_call = "cant end turn";
            turn = false;
            Comm.tellDE2UserEndedTurn(pid);
        } else {
            // TODO: tell de2 that my turn is over
            turn = false;
            Comm.tellDE2UserEndedTurn(pid);
        }
    }

    public void forceEndTurn() {
        // TODO: tell de2 that my turn is over
        test_call = "forceEndTurn";
        turn = false;
        Comm.tellDE2UserEndedTurn(pid);
    }

    public int getFixedRange() {
        int range = 1;
        for (Card c : cc.getBlueCards()) {
            if (c.name.compareTo(SCOPE) == 0) {
                range++;
            }
        }
        return range;
    }

    // Get maximum range of the payer
    public int getRange() {
        int range = 1;
        for (Card c : cc.getBlueCards()) {
            if (c.name.compareTo(SCOPE) == 0) {
                range++;
            } else if (c.isGunCard()) {
                range += c.onePlayerFixed;
                range--;
            }
        }
        return range;
    }

    public void playCard(int cid, Integer target) {
        if (turn) {
            Card c = cc.getHandCard(cid);
            if (c == null) {
                // This should never occur
                test_call = "card not in hand";
            } else {
                if (c.zap) {
                    if (c.onePlayerReachable) { // zap
                        if (!zappedThisTurn) {
                            int pid = target.intValue();
                            if (checkRange(pid)) {
                                zapOpponent(pid);
                                // zappedThisTurn = true;
                                cc.discardCard(cid);
                            } else {
                                // TODO Tony: target not in range, give player
                                // appropriate message
                                test_call = "target not in range";
                            }
                        } else {
                            // TODO Tony: tell user he has already zapped
                            test_call = "cant zap twice";
                        }
                    } else if (c.allPlayers) { // gatling
                        zapAll();
                        cc.discardCard(cid);
                    }
                } else if (c.life == 1) {
                    if (c.allPlayers) { // saloon
                        goToSaloon();
                        cc.discardCard(cid);
                    } else if (lives < maxLives) { // beer
                        drinkBeer();
                        cc.discardCard(cid);
                    } else {
                        // TODO Tony: tell user he has full lives, so he can't
                        // play beer
                        test_call = "cant beer";
                    }
                } else if (c.border == 'L') {
                    if (c.name.compareTo(JAIL) == 0) { // space jail
                        int pid = target.intValue();
                        Opponent o = opponents.get(Integer.valueOf(pid));
                        if (o.getRole().compareTo(SHERIFF) == 0) {
                            // TODO Tony: tell user he can't jail the sheriff
                            test_call = "cant jail sheriff";
                        } else {
                            for (Card t : o.getBlueCards()) {
                                if (t.name.compareTo(JAIL) == 0) {
                                    // TODO Tony: tell user he can't jail
                                    // someone who is already in jail
                                    test_call = "cant jail jailed";
                                    return;
                                }
                            }
                            throwInJail(pid, cid);
                            cc.discardCard(cid);
                        }
                    } else { // the other blue cards
                        cc.placeBlueCard(cid);
                        test_call = "blue card";
                    }
                } else if (c.missed) { // missed
                    // TODO Tony: tell user he can't play a missed card during
                    // his turn (it has no effect)
                    test_call = "cant play missed";
                } else if (c.draw > 0) {
                    if (c.onePlayerFixed == 1) { // panic
                        int pid = target.intValue();
                        if (checkFixedRange(pid)) {
                            panicOpponent(pid);
                            cc.discardCard(cid);
                        } else {
                            // TODO Tony: target not in range, give player
                            // appropriate message
                            test_call = "cant panic out of range";
                        }
                    } else { // draw card
                        drawCards(c.draw);
                        cc.discardCard(cid);
                    }
                } else if (c.forceDiscard) {
                    if (c.onePlayer) { // cat balou
                        catBalouOpponent(1);
                        cc.discardCard(cid);
                    }
                } else if (c.name.compareTo(DUEL) == 0) {
                    int pid = target.intValue();
                    duelOpponent(pid);
                    cc.discardCard(cid);
                } else if (c.name.compareTo(ALIENS) == 0) {
                    releaseTheAliens();
                    cc.discardCard(cid);
                } else if (c.name.compareTo(GENERAL_STORE) == 0) {
                    generalStore();
                    cc.discardCard(cid);
                }
            }
        } else {
            // TODO Tony: tell player it isn't his turn
            test_call = "Not turn";
        }
    }

    // //////////////////////////////////////////////////
    // THE BELOW PUBLIC FUNCTIONS ARE CALLED WHEN AN OPPONENT INITIATES AN
    // ACTION

    // TEST THEM ALL
    public void onZap() {
        // TODO: user has choice of playing miss or taking a life (player
        // getting zapped)
        // TODO: let him use a beer if this is a lethal hit
        activity.runOnUiThread(new Runnable() {
            public void run() {
                int missed_cid = 0;
                for (Card c : getHandCards()) {
                    if (c.missed) {
                        missed_cid = c.cid;
                        break;
                    }
                }
                CharSequence choices[];
                if (missed_cid != 0) {
                    choices = new CharSequence[] { "Take hit", "Use miss" };
                } else {
                    choices = new CharSequence[] { "Take hit" };
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Player.activity);
                builder.setTitle("Select an option");
                builder.setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Boolean userplayedmiss = false;

                        int missed_cid = 0;
                        for (Card c : getHandCards()) {
                            if (c.missed) {
                                missed_cid = c.cid;
                                break;
                            }
                        }

                        if (which == 0) {
                            userplayedmiss = false;
                        } else {
                            userplayedmiss = true;
                        }

                        if (userplayedmiss) {
                            discardCard(missed_cid);
                            Comm.tellDE2CardsInHand(pid, getNumberOfHandCards(), getHandCards());
                        } else {
                            setLives(lives - 1);
                            // Don't need to send msg because setLives
                            // already sends one
                            // Comm.tellDE2UserUpdateLives(pid, lives);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    public void onAliens() {
        // TODO: user has choice of playing zap or taking a life (player getting
        // dueled or aliens)
        // TODO: let him use a beer if this is a lethal hit
        activity.runOnUiThread(new Runnable() {
            public void run() {
                int zap_cid = 0;
                for (Card c : getHandCards()) {
                    if (c.zap) {
                        zap_cid = c.cid;
                        break;
                    }
                }

                CharSequence choices[];
                if (zap_cid != 0) {
                    choices = new CharSequence[] { "Take hit", "Use zap" };
                } else {
                    choices = new CharSequence[] { "Take hit" };
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Player.activity);
                builder.setTitle("Select an option");
                builder.setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Boolean userplayedzap = false;

                        int zap_cid = 0;
                        for (Card c : getHandCards()) {
                            if (c.zap) {
                                zap_cid = c.cid;
                                break;
                            }
                        }

                        if (which == 0) {
                            userplayedzap = false;
                        } else {
                            userplayedzap = true;
                        }

                        if (userplayedzap) {
                            discardCard(zap_cid);
                            Comm.tellDE2CardsInHand(pid, getNumberOfHandCards(), getHandCards());
                        } else {
                            setLives(lives - 1);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    public void onSaloon() {
        if (lives < maxLives) {
            setLives(lives + 1);
            ;
        }
    }

    public void onPanic() {
        // show dialog to pick
        // cards are in:
        activity.runOnUiThread(new Runnable() {
            public void run() {
                CharSequence choices[] = new CharSequence[DE2Message.getCard_choices().size()];
                int i = 0;
                for (Integer cid : DE2Message.getCard_choices()) {
                    choices[i++] = CardController.getValidCard(cid.intValue()).name;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Player.activity);
                builder.setTitle("Select an card");
                builder.setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int cid = DE2Message.getCard_choices().get(which);
                        Comm.tellDE2UserTransferCard(pid, cid);
                    }
                });
                builder.show();
            }
        });
    }

    public void onCatBalou() {
        // show dialog to discard
        // cards are in:
        activity.runOnUiThread(new Runnable() {
            public void run() {
                CharSequence choices[] = new CharSequence[DE2Message.getCard_choices().size()];
                int i = 0;
                for (Integer cid : DE2Message.getCard_choices()) {
                    choices[i++] = CardController.getValidCard(cid.intValue()).name;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Player.activity);
                builder.setTitle("Select an card");
                builder.setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int cid = DE2Message.getCard_choices().get(which);
                        Comm.tellDE2UserPickedCard(pid, cid);
                    }
                });
                builder.show();
            }
        });
    }

    public void onJail(int cid) {
        // Nothing needs to be done
        receiveBlueCard(cid);
        Comm.tellDE2BlueCardsInFront(pid, getNumberOfBlueCards(), getBlueCards());
    }

    public void onDuel() {
        // TODO: user has choice of playing zap or taking a life (player getting
        // dueled or aliens)
        // TODO: let him use a beer if this is a lethal hit
        activity.runOnUiThread(new Runnable() {
            public void run() {
                int zap_cid = 0;
                for (Card c : getHandCards()) {
                    if (c.zap) {
                        zap_cid = c.cid;
                        break;
                    }
                }

                CharSequence choices[];
                if (zap_cid != 0) {
                    choices = new CharSequence[] { "Take hit", "Use zap" };
                } else {
                    choices = new CharSequence[] { "Take hit" };
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Player.activity);
                builder.setTitle("Select an option");
                builder.setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Boolean userplayedzap = false;

                        int zap_cid = 0;
                        for (Card c : getHandCards()) {
                            if (c.zap) {
                                zap_cid = c.cid;
                                break;
                            }
                        }

                        if (which == 0) {
                            userplayedzap = false;
                        } else {
                            userplayedzap = true;
                        }

                        if (userplayedzap) {
                            discardCard(zap_cid);
                            Comm.tellDE2CardsInHand(pid, getNumberOfHandCards(), getHandCards());
                        } else {
                            setLives(lives - 1);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    public void onGeneralStore() {
        // pick something from card_choices;
        // show dialog to pick
        // cards are in:
        activity.runOnUiThread(new Runnable() {
            public void run() {
                CharSequence choices[] = new CharSequence[DE2Message.getCard_choices().size()];
                int i = 0;
                for (Integer cid : DE2Message.getCard_choices()) {
                    choices[i++] = CardController.getValidCard(cid.intValue()).name;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Player.activity);
                builder.setTitle("Select an card");
                builder.setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int cid = DE2Message.getCard_choices().get(which);
                        Comm.tellDE2UserPickedCard(pid, cid);
                    }
                });
                builder.show();
            }
        });
    }

    public void onUpdateCards() {
        Comm.tellDE2CardsInHand(pid, getNumberOfHandCards(), getHandCards());
    }

    public void onLoseCard(int cid) {
        Boolean isHand = false;
        for (Card c : getHandCards()) {
            if (c.cid == cid) {
                isHand = true;
            }
        }
        discardCard(cid);
        if (isHand) {
            Comm.tellDE2CardsInHand(pid, getNumberOfHandCards(), getHandCards());
        } else {
            Comm.tellDE2BlueCardsInFront(pid, getNumberOfBlueCards(), getBlueCards());
        }
    }

    public void onReceiveCard(int cid) {
        receiveCard(cid);

        activity.runOnUiThread(new Runnable() {
            public void run() {
                PlayerHandCards playerHandCards;
                if (playerActivity != null) {
                    playerHandCards = (PlayerHandCards) playerActivity.getSupportFragmentManager().findFragmentByTag(((PlayerActivity) activity).getTabHandCards());
                    playerHandCards.buildCards();
                }
            }
        });

        Comm.tellDE2CardsInHand(pid, getNumberOfHandCards(), getHandCards());
    }

    public void onReceiveRoleAndPid(int fromId, String role) {
        setPid(fromId);
        setRole(role);
        Comm.tellDE2OK(pid);
    }

    // /////////////////////////////////////////////////////

    public void zapOpponent(int pid) {
        // TODO: tell de2 that this player wants to zap the opponent with
        // playerid=pid
        // This function shouldn't return until either the opponent uses missed,
        // beer, or takes the hit
        test_call = "zapOpponent";
        Comm.tellDE2UserUsedOther(this.pid, pid, "ZAP", 0);

        return;
    }

    public void zapAll() {
        // TODO: tell de2 that this player wants to zap everyone
        // This function shouldn't return until every opponent uses missed,
        // beer, or takes the hit
        test_call = "zapAll";
        Comm.tellDE2UserUsedSelf(this.pid, "GATLING");

        return;
    }

    public void goToSaloon() {
        // TODO: tell de2 that everyone gets a life
        // This function shouldn't return until de2 says everything is good
        test_call = "goToSaloon";
        Comm.tellDE2UserUsedSelf(this.pid, "SALOON");

        return;
    }

    public void drinkBeer() {
        // TODO: tell de2 that you beered
        // This function shouldn't return until de2 says everything is good
        test_call = "drinkBeer";
        setLives(lives + 1);
        Comm.tellDE2UserUsedSelf(this.pid, "BEER");

        return;
    }

    public void throwInJail(int pid, int cid) {
        // TODO: tell de2 that this opponent should be put in jail
        // This function shouldn't return until de2 says everything is good
        test_call = "throwInJail";
        Comm.tellDE2UserUsedOther(this.pid, pid, "JAIL", cid);

        return;
    }

    public void drawCards(int numCards) {
        // TODO: tell de2 that this player needs numCards cards
        // This function shouldn't return until de2 says everything is good
        test_call = "drawCards";
        Comm.tellDE2UserNeedsXCards(this.pid, numCards);

        return;
    }

    // public void drawOneCard() {
    // // TODO: tell de2 that this player needs 1 card
    // test_call = "drawOneCard";
    // Comm.tellDE2UserNeedsXCards(this.pid, 1);
    // // TODOCOLIN: get card
    // Boolean once = true;
    // DE2Message.setReadyToContinue(false);
    // while (!DE2Message.getReadyToContinue(once)) {
    // if (once) {
    // Log.i("colin", "Waiting for readyToContinue");
    // once = false;
    // }
    // }
    // DE2Message.setReadyToContinue(false);
    // return;
    // }

    public void panicOpponent(int pid) {
        // TODO: tell de2 that this player wants to panic opponent
        // This function shouldn't return until de2 says everything is good
        test_call = "panicOpponent";
        Comm.tellDE2UserUsedOther(this.pid, pid, "PANIC", 0);

        return;
    }

    public void catBalouOpponent(int pid) {
        // TODO: tell de2 to randomly discard card from all opponents
        // This function shouldn't return until de2 says everything is good
        test_call = "catBalouOpponentCard";
        Comm.tellDE2UserUsedOther(this.pid, pid, "CAT_BALOU", 0);

        return;
    }

    public void duelOpponent(int pid) {
        // TODO: tell de2 to begin duel with opponent
        // This function shouldn't return until de2 says everything is good
        test_call = "duelOpponent";
        Comm.tellDE2UserUsedOther(this.pid, pid, "DUEL", 0);

        return;
    }

    public void releaseTheAliens() {
        // TODO: tell de2 to send aliens after everyone
        // This function shouldn't return until de2 says everything is good
        test_call = "releaseTheAliens";
        Comm.tellDE2UserUsedSelf(this.pid, "ALIENS");

        return;
    }

    public void generalStore() {
        // TODO: tell de2 to use general store
        // This function shouldn't return until de2 says everything is good
        test_call = "generalStore";
        Comm.tellDE2UserUsedSelf(this.pid, "GENERAL_STORE");

        return;
    }

    private boolean checkFixedRange(int pid) {
        Opponent o = opponents.get(Integer.valueOf(pid));
        if (o.getRange() <= getFixedRange()) {
            return true;
        } else {
            return false;
        }
    }

    // Check if opponent if within range to shoot
    public boolean checkRange(int pid) {
        Opponent o = opponents.get(Integer.valueOf(pid));
        if (o.getRange() <= getRange()) {
            return true;
        } else {
            return false;
        }
    }
}
