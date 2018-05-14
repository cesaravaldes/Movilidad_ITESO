
/*I2C Mode*/
  #include <Wire.h>
  #include <PN532_I2C.h>
  #include <PN532.h>
  #include <NfcAdapter.h>
  
  PN532_I2C pn532i2c(Wire);
  PN532 nfc(pn532i2c);

#define GREEN_LED 8
#define RED_LED 7

void setup(void) {
  Serial.begin(9600);
  //Serial.println("Hello!");

  nfc.begin();

  uint32_t versiondata = nfc.getFirmwareVersion();
  if (! versiondata) {
//    Serial.print("Didn't find PN53x board");
    while (1); // halt
  }
  /*
  // Got ok data, print it out!
  Serial.print("Found chip PN5"); Serial.println((versiondata>>24) & 0xFF, HEX); 
  Serial.print("Firmware ver. "); Serial.print((versiondata>>16) & 0xFF, DEC); 
  Serial.print('.'); Serial.println((versiondata>>8) & 0xFF, DEC);
  */
  // Set the max number of retry attempts to read from a card
  // This prevents us from waiting forever for a card, which is
  // the default behaviour of the PN532.
  nfc.setPassiveActivationRetries(0xFF);
  
  // configure board to read RFID tags
  nfc.SAMConfig();
    /*
  Serial.println("Waiting for an ISO14443A card");
  */
  pinMode(GREEN_LED,OUTPUT);
  pinMode(RED_LED,OUTPUT);
  
}

void loop(void) {
  boolean success;
  uint8_t uid[] = { 0, 0, 0, 0, 0, 0, 0 };  // Buffer to store the returned UID
  uint8_t uidLength;                        // Length of the UID (4 or 7 bytes depending on ISO14443A card type)


  success = nfc.readPassiveTargetID(PN532_MIFARE_ISO14443A, &uid[0], &uidLength);
  String data_to_send;
  if (success) {

    //Takes the UID value and add them to the string to be send
    for (uint8_t i=0; i < uidLength; i++) 
    {
       data_to_send += uid[i];
    }

    //adds the identifier of the action and the gate
    data_to_send += ",entrada,sur";
      Serial.print(data_to_send); 
    
    Serial.println("");
    char validated = 'y';
    //Cleans the serial
    Serial.flush();
    char Access = Serial.read();

    //blocks the execution until a value is received from the serial port
    while(Access != 'y' && Access != 'n'){
       Access = Serial.read();
    }

    //Compares the received value to the predifined authorized access values
    if ( validated == Access){
      digitalWrite(GREEN_LED,HIGH);
    }else{
      digitalWrite(RED_LED,HIGH);
    }
    //Waits for 1 second to make visible the light of the LED
    delay(1000);

    //Turns off both LEDs just to not make another if comparation
    digitalWrite(RED_LED,LOW);
    digitalWrite(GREEN_LED,LOW);
    
  }
  else
  {
    // PN532 probably timed out waiting for a card
  }
}
