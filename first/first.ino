const int switchPin = 3;   // Switch connected to pin 2
const int ledPin = 10;      // LED connected to pin 9

void setup() {
    pinMode(switchPin, INPUT_PULLUP); // Internal pull-up resistor
    pinMode(ledPin, OUTPUT);
}

void loop() {
    int buttonState = digitalRead(switchPin);
    
    if (buttonState == LOW) {  // Button pressed (since using INPUT_PULLUP)
        digitalWrite(ledPin, HIGH); // Turn LED on
    } else {
        digitalWrite(ledPin, LOW);  // Turn LED off
    }
}