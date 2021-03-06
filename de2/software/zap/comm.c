#include "comm.h"

alt_up_rs232_dev* init_clear_uart(Comm_data* cd) {
    printf("UART Initialization\n");
    alt_up_rs232_dev* uart = alt_up_rs232_open_dev("/dev/rs232");

    printf("Clearing read buffer to start\n");
    while (alt_up_rs232_get_used_space_in_read_FIFO(uart)) {
        alt_up_rs232_read_data(uart, &(cd->data), &(cd->parity));
    }

    return uart;
}

void receive_data_from_middleman(alt_up_rs232_dev* uart, Comm_data* cd) {
    // Now receive the message from the Middleman

    // First byte is the client id received
    while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0);
    alt_up_rs232_read_data(uart, &(cd->data), &(cd->parity));
    cd->client_id = (int)cd->data;
    // Second byte is the number of characters passed by Middleman, don't store
    while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0);
    alt_up_rs232_read_data(uart, &(cd->data), &(cd->parity));
    // Third byte is the number of characters in our message
    while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0);
    alt_up_rs232_read_data(uart, &(cd->data), &(cd->parity));
    cd->num_to_receive = (int)cd->data;
    cd->r_len = cd->num_to_receive;

    int i;
    for (i = 0; i < cd->num_to_receive; i++) {
        while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0);
        alt_up_rs232_read_data(uart, &(cd->data), &(cd->parity));

        cd->r_message[i] = cd->data;
    }
    for (i = 0; i < cd->num_to_receive; i++) {
    }

    // Acknowledge message received
    if (0) {
		alt_up_rs232_write_data(uart, (unsigned char) cd->client_id);
		alt_up_rs232_write_data(uart, 1);
		alt_up_rs232_write_data(uart, 0x0a);
		usleep(1500000);
    }
}

void send_data_to_middleman(alt_up_rs232_dev* uart, Comm_data* cd) {
	usleep(100000);
    // Reply to the message
    // Write the client id
    alt_up_rs232_write_data(uart, (unsigned char) cd->client_id);
    // Write the message length
    alt_up_rs232_write_data(uart, cd->s_len);
    // Write the message
    int i;
    for (i = 0; i < cd->s_len; i++) {
        alt_up_rs232_write_data(uart, cd->s_message[i]);
    }
}
