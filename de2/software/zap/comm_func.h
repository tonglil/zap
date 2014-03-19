#ifndef COMM_FUNC_H_
#define COMM_FUNC_H_

#include <stdio.h>
#include <stdlib.h>
#include "altera_up_avalon_rs232.h"
#include <string.h>

#include "Player.h"
#include "comm.h"

void tell_user_pid_role(int pid, int role);

void tell_user_all_opponent_range_role(int pid, PlayersInfo* pi);

void tell_user_all_opponent_blue_lives(int pid, PlayersInfo* pi);

void tell_user_new_card(int pid, int cid);

void tell_user_lost_card(int pid, int cid);

void tell_user_their_turn(int pid);

void tell_user_play_or_lose_life(int pid);

void tell_user_zap_or_lose_life(int pid);

void tell_user_get_life(int pid);

#endif