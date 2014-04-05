package com.zap.main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import android.util.Log;

import com.zap.MyApplication;

public class Comm {
    private static MyApplication app;

    public static MyApplication getApp() {
        return app;
    }

    public static void setApp(MyApplication app) {
        Comm.app = app;
    }

    // Called when the user wants to send a message
    public static void sendMessage(String message) {
        // Send Message Structure:
        // [0] length not including [0]
        // [1] message type
        Log.i("colin", "Waiting for Ack");
        while (!DE2Message.isReadyToSend())
            ;
        DE2Message.setReadyToSend(false);
        Log.i("colin", "Got Ack");

        String msg = message;
        byte[] bmsg = hstba(msg);

        // Create an array of bytes. First byte will be the
        // message length, and the next ones will be the message
        byte buf[] = new byte[bmsg.length + 1];

        buf[0] = (byte) bmsg.length;
        System.arraycopy(bmsg, 0, buf, 1, bmsg.length);

        // Now send through the output stream of the socket

        OutputStream out;
        try {
            out = app.sock.getOutputStream();
            try {
                out.write(buf, 0, bmsg.length + 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessageNoAck(String message) {
        // Send Message Structure:
        // [0] length not including [0]
        // [1] message type
        String msg = message;
        byte[] bmsg = hstba(msg);

        // Create an array of bytes. First byte will be the
        // message length, and the next ones will be the message
        byte buf[] = new byte[bmsg.length + 1];

        buf[0] = (byte) bmsg.length;
        System.arraycopy(bmsg, 0, buf, 1, bmsg.length);

        // Now send through the output stream of the socket

        OutputStream out;
        try {
            out = app.sock.getOutputStream();
            try {
                out.write(buf, 0, bmsg.length + 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // bytesToHexString
    final protected static char[] hexArray = "0123456789abcdef".toCharArray();

    public static String bth(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    // hexStringToByteArray

    public static byte[] hstba(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // intToHexString

    public static String iths(int intg) {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toHexString(intg));
        if (sb.length() < 2) {
            sb.insert(0, '0'); // pad with leading zero if needed
        }
        String hex = sb.toString();
        return hex;
    }

    public static void tellDE2CardsInHand(int pid, int ncards, ArrayList<Card> cards) {
        String msg = "11" + iths(pid) + iths(ncards);
        for (Card c : cards) {
            msg = msg + iths(c.cid);
        }
        Log.i("colin", "Going to send message");
        sendMessage(msg);
        Log.i("colin", "Sent message");
        return;
    }

    public static void tellDE2BlueCardsInFront(int pid, int ncards, ArrayList<Card> cards) {
        String msg = "12" + iths(pid) + iths(ncards);

        for (Card c : cards) {
            msg = msg + iths(c.cid);
        }

        sendMessage(msg);

        return;
    }

    public static void tellDE2UserUsedSelf(int pid, String type) {
        int itype = 0;
        if (type.equals("BEER")) {
            itype = 0x01;
        } else if (type.equals("GATLING")) {
            itype = 0x02;
        } else if (type.equals("ALIENS")) {
            itype = 0x03;
        } else if (type.equals("GENERAL_STORE")) {
            itype = 0x04;
        } else if (type.equals("SALOON")) {
            itype = 0x05;
        }
        String msg = "13" + iths(pid) + iths(itype);

        sendMessage(msg);

        return;
    }

    public static void tellDE2UserUsedOther(int pid, int pid1, String type, int cid) {
        if (pid == pid1) {
            DE2Message.setDoingToSelf(true);
        } else {
            DE2Message.setDoingToSelf(false);
        }
        int itype = 0;
        if (type.equals("ZAP")) {
            itype = 0x01;
        } else if (type.equals("PANIC")) {
            itype = 0x02;
        } else if (type.equals("CAT_BALOU")) {
            itype = 0x03;
        } else if (type.equals("DUEL")) {
            itype = 0x04;
        } else if (type.equals("JAIL")) {
            itype = 0x05;
        }

        String msg = "14" + iths(pid) + iths(pid1) + iths(itype) + iths(cid);

        sendMessage(msg);

        return;
    }

    public static void tellDE2UserEndedTurn(int pid) {
        String msg = "15" + iths(pid);

        sendMessage(msg);

        return;
    }

    public static void tellDE2UserNeedsXCards(int pid, int ncards) {
        String msg = "16" + iths(pid) + iths(ncards);

        sendMessageNoAck(msg);

        return;
    }

    public static void tellDE2UserUpdateLives(int pid, int lives) {
        String msg = "17" + iths(pid) + iths(lives);
        Log.i("colin", "tellDE2UserUpdateLives " + msg);
        sendMessage(msg);

        return;
    }

    public static void tellDE2UserPickedCard(int pid, int cid) {
        String msg = "18" + iths(pid) + iths(cid);

        sendMessage(msg);

        return;
    }

    public static void tellDE2UserTransferCard(int pid, int cid) {
        String msg = "19" + iths(pid) + iths(cid);

        sendMessage(msg);

        return;
    }

    public static void tellDE2OK(int pid) {
        String msg = "1a" + iths(pid);

        sendMessageNoAck(msg);

        return;
    }

    public static void tellDE2Connected(int pid) {
        String msg = "1b" + iths(pid);

        sendMessage(msg);

        return;
    }

    public static void receiveInterpretDE2(byte buf[], Player p) {
        // Message structure for Acknowledgment
        // [0] 0x0a
        if (buf[0] == 0x0a) {
            DE2Message.setReadyToSend(true);
            Log.i("colin", "DE2 acknowledged, Android can send more");
            return;
        }

        // Message structure for Instructions:
        // [0] Client_Id
        // [1] S_len
        // [2] Message_type

        int l = 0;
        // int length = buf[l++];
        int fromId = buf[l++];
        int m_len = buf[l++];
        int type = buf[l++];
        int toId = fromId;
        int count = 0;
        ArrayList<ArrayList<Integer>> r_pinfo = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> r_cinfo = new ArrayList<ArrayList<Integer>>();
        switch (type) {
        case 0x01: {
            // tell_user_pid_role
            // [3] role
            int int_role = (int) buf[l++];
            String role = "NONE";
            switch (int_role) {
            case 0x00: {
                role = "NONE";
                break;
            }
            case 0x01: {
                role = "SHERIFF";
                break;
            }
            case 0x02: {
                role = "DEPUTY";
                break;
            }
            case 0x03: {
                role = "OUTLAW";
                break;
            }
            case 0x04: {
                role = "RENEGADE";
                break;
            }
            }
            int life = 0;
            if (role.equals("SHERIFF")) {
                life = 5;
            } else {
                life = 4;
            }
            p.onReceiveRoleAndPid(fromId, role, life);
            break;
        }
        case 0x02: {
            // tell_user_all_opponent_range_role
            // [3] pid
            // [4] range
            // [5] role
            // ...
            // [21] pid
            // [22] range
            // [23] role
            for (int i = 0; i < 7; i++) {
                int pid = (int) buf[3 * i + l + 1];
                int range = (int) buf[3 * i + l + 1];
                int int_role = (int) buf[3 * i + l + 2];
                if (p.getPid() == pid) {
                    break;
                }
                String role = "NONE";
                switch (int_role) {
                case 0x01: {
                    role = "SHERIFF";
                    break;
                }
                case 0x02: {
                    role = "DEPUTY";
                    break;
                }
                case 0x03: {
                    role = "OUTLAW";
                    break;
                }
                case 0x04: {
                    role = "RENEGADE";
                    break;
                }
                default: {
                    role = "NONE";
                    break;
                }
                }
                p.setOpponentRole(pid, role);
                p.setOpponentRange(pid, range);
            }
            Log.i("colin", "set roles and ranges");
            Comm.tellDE2OK(fromId);
            break;
        }
        case 0x03: {
            // tell_user_all_opponent_blue_lives
            // [3] pid
            // [4] lives
            // [5] num_blues
            // ...
            // [21] pid
            // [22] lives
            // [23] num_blues
            // [24] rest of array is cid for blue cards
            // ...
            int i;
            for (i = 0; i < 7; i++) {
                int pid = (int) buf[3 * i + l];
                int lives = (int) buf[3 * i + l + 1];
                int num_blues = (int) buf[3 * i + l + 2];

                if (p.getPid() == pid) {
                    break;
                }

                p.setOpponentLives(pid, lives);
                ArrayList<Integer> pinfo = new ArrayList<Integer>();
                pinfo.add(num_blues);
                r_pinfo.add(pinfo);
            }
            int k = 3 * i + l;
            for (i = 0; i < 7; i++) {
                if (p.getPid() == i) {
                    break;
                }

                ArrayList<Integer> bcard = new ArrayList<Integer>();
                for (int j = 0; j < r_pinfo.get(i).get(0); j++) {
                    bcard.add((int) buf[k++]);
                }
                p.setOpponentBlueCards(i, bcard);
            }
            Log.i("colin", "set lives and blues");
            Comm.tellDE2OK(fromId);
            break;
        }
        case 0x04: {
            // tell_user_new_card
            // [3] cid
            int cid = (int) buf[l++];
            p.onReceiveCard(cid);
            break;
        }
        case 0x05: {
            // tell_user_lost_card
            // [3] cid
            int cid = (int) buf[l++];
            p.onLoseCard(cid);
            break;
        }
        case 0x06:
            // tell_user_their_turn
            Log.i("colin", p.getPid() + " turn started outside");
            p.startTurn();
            Log.i("colin", p.getPid() + " turn started outside return");
            break;
        case 0x07: {
            // tell_user_miss_or_lose_life
            Log.i("colin", "Try to onZap");
            p.onZap();
            break;
        }
        case 0x08: {
            // tell_user_zap_or_lose_life
            // [3] 0x01 for Aliens, 0x02 for Duel
            int choice = (int) buf[l++];
            if (choice == 0x01) {
                p.onAliens();
            } else {
                p.onDuel();
            }
            break;
        }
        case 0x09: {
            // tell_user_get_life
            p.onSaloon();
            break;
        }
        case 0x2a:
            // tell_user_ok
            DE2Message.setReadyToContinue(true);
            break;
        case 0x0b: {
            // tell_user_blue_player_infront
            // [3] cid
            int cid = (int) buf[l++];
            p.receiveBlueCard(cid);
            break;
        }
        case 0x0c: {
            // tell_user_store
            // [3] ncards
            // [4] array for choice of cards, length depends on ncards
            // ..
            int ncards = (int) buf[l++];
            ArrayList<Integer> card_choices = new ArrayList<Integer>();
            for (int i = 0; i < ncards; i++) {
                int cid = (int) buf[l++];
                card_choices.add(cid);
            }
            p.onGeneralStore(card_choices);
        }
        case 0x0d: {
            // tell_user_panic
            // [3] toId
            // [4] nbcards
            // [5] ncards
            // [6] array of blue cards
            // ...
            // [6 + nbcards] array of cards
            // ...
            toId = (int) buf[l++];
            int nbcards = (int) buf[l++];
            int ncards = (int) buf[l++];
            ArrayList<Integer> card_choices = new ArrayList<Integer>();
            for (int i = 0; i < nbcards; i++) {
                int cid = (int) buf[l++];
                card_choices.add(cid);
            }
            for (int i = 0; i < ncards; i++) {
                int cid = (int) buf[l++];
                card_choices.add(cid);
            }
            r_cinfo.add(card_choices);
            p.onPanic();
        }
        case 0x0e: {
            // tell_user_cat_balou
            // [3] toId
            // [4] nbcards
            // [5] ncards
            // [6] array of blue cards
            // ...
            // [6 + nbcards]
            // ...
            toId = (int) buf[l++];
            int nbcards = (int) buf[l++];
            int ncards = (int) buf[l++];
            ArrayList<Integer> card_choices = new ArrayList<Integer>();
            for (int i = 0; i < nbcards; i++) {
                int cid = (int) buf[l++];
                card_choices.add(cid);
            }
            for (int i = 0; i < ncards; i++) {
                int cid = (int) buf[l++];
                card_choices.add(cid);
            }
            r_cinfo.add(card_choices);
            p.onCatBalou();
        }
        case 0x0f: {
            // tell_user_jail
            // [3] toId
            // [4] cid of jailcard
            toId = (int) buf[l++];
            int cid = (int) buf[l++];
            p.onJail(cid);
        }
        default:
            break;
        }
        if (type == 0x05 || type == 0x06 || type == 0x07 || type == 0x08 || type == 0x09 || type == 0x0b) {
            DE2Message.setMessage(false, type, fromId, toId, count, r_pinfo, r_cinfo);
        } else {
            DE2Message.setMessage(true, type, fromId, toId, count, r_pinfo, r_cinfo);
        }
        return;
    }
}