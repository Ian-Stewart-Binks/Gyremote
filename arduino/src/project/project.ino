#include <SoftwareSerial.h>

#define uint8 unsigned char 
#define uint16 unsigned int
#define uint32 unsigned long int

#define RxD 6 // Send
#define TxD 7 // Recieve
 
SoftwareSerial blueToothSerial(RxD,TxD);

float compensation_right = 1;
float compensation_left  = 1;

int lightOn = 0;

void setup() { 
    Serial.begin(9600);
    pinMode(RxD, INPUT); 
    pinMode(TxD, OUTPUT);
    setupBlueToothConnection(); 
  
    pinMode(12, OUTPUT);
    pinMode(9,  OUTPUT); 

    pinMode(13, OUTPUT);
    pinMode(8,  OUTPUT);
    pinMode(4,  OUTPUT);
    pinMode(5,  OUTPUT);
    digitalWrite(4, LOW);
    
    pinMode(2,  OUTPUT);
    pinMode(A0, INPUT);
    
    digitalWrite(2, HIGH);
}

short left         = 0;
short right        = 0;
int frontVal       = 0;
int backVal        = 0;
int rightVal       = 0;
int leftVal        = 0;
int frontCounter   = 0;
int backCounter    = 0;
int disable        = 0;
int rightCounter   = 0;
int leftCounter    = 0;

int frontFlag      = 0;
int backFlag       = 0;

int leftFlag       = 0;
int rightFlag      = 0;

int autoLeftFlag   = 0;
int autoRightFlag  = 0;

int forwardFlag    = 0;
int reverseFlag    = 0;

int autopilot      = 0;
int reverseCounter = 0;
int turnCounter    = 0;
int turnFlag       = 0;

void loop() { 
    digitalWrite(13, LOW);
    char recvChar;
    while (1) {
        frontVal = analogRead(A5);
        backVal  = analogRead(A4);
        rightVal = analogRead(A3);
        leftVal  = analogRead(A2);

        frontCounter++;
        backCounter++;
        
        if (reverseFlag) { 
            reverseCounter++;
        }
        
        if (turnFlag) {
          turnCounter++;  
        }
        
        rightCounter++;
        leftCounter++;
        
        if (autopilot == 1 && reverseCounter == 2000 && reverseFlag) {
            reverseCounter = 0;
            if (autoLeftFlag == 1 && autoRightFlag == 0) {
                if (frontFlag == 0) { 
                    goRight();
                }
            } else if (autoLeftFlag == 0 && autoRightFlag == 1) {
                if (frontFlag == 0) { 
                    goLeft();
                }
            } else if (autoLeftFlag == 0 && autoRightFlag == 0) {
                if (frontFlag == 0) { 
                    goRight();
                }
            
            } else if (autoLeftFlag == 1 && autoRightFlag == 1) {
                if (frontFlag == 0) {
                    goRight();
                }
            }
            
            autoRightFlag = 0;
            autoLeftFlag  = 0;
        }
        
        if (autopilot == 1 && turnCounter == 1000 && turnFlag) {
            turnFlag = 0;
            goForward(1);
        }
        
        
        if (frontVal > 400) {
            if (frontCounter > 1000) {
                blueToothSerial.print("1");
                Serial.println("1");
                frontCounter = 0;
            }
            if (frontFlag == 0) {
                blueToothSerial.print("1");
                
                brake();
                
                if (autopilot == 1) {
                    reverse(1);
                }
                
                frontFlag = 1;
            }
        }
        
         
       if (backVal > 400) {
            if (backCounter > 1000) {
                blueToothSerial.print("3");
                backCounter = 0;
            }
            if (backFlag == 0) {
              blueToothSerial.print("3");
              
              brake();
              if (autopilot == 1) {
                  goForward(1);
              }
              backFlag = 1;
            }
        
       } 
       
       if (rightVal > 400) {
            if (rightCounter > 1000) {
                blueToothSerial.print("5");
                Serial.print("5");
                Serial.println("5");
                rightCounter = 0;
            }
            if (rightFlag == 0) {
                blueToothSerial.print("5");
                Serial.print("5");
                Serial.println("5");
                brake();
                if (autopilot == 1) {
                    reverse(1);
                }
                rightFlag     = 1;
                autoRightFlag = 1;
            }
        }
        
         
       if (leftVal > 400) {
            if (leftCounter > 1000) {
                blueToothSerial.print("7");
                leftCounter = 0;
            }
            if (leftFlag == 0) {
              blueToothSerial.print("7");
              brake();
              if (autopilot == 1) {
                    reverse(1);
              }
              leftFlag     = 1;
              autoLeftFlag = 1;
            }
        
       } 
       
       
       if (backVal < 300 && frontVal < 300 && leftVal < 300 && rightVal < 300) {
            if (frontFlag == 1 || backFlag == 1) {
                blueToothSerial.print("6");
                blueToothSerial.print("4");
                blueToothSerial.print("2");
                blueToothSerial.print("0");
                Serial.println("0");
                unbrake();
            }
            backFlag = 0;
            frontFlag = 0;
            rightFlag = 0;
            leftFlag = 0;
        } 
        
        if (backVal < 300) {
            if (backCounter > 1000) {
                blueToothSerial.print("2");
                backCounter = 0;
            }
            
            if (backFlag == 1) {
                blueToothSerial.print("2");
            }
            backFlag = 0;
        }
        
        if (frontVal < 300) {
            if (frontCounter > 1000) {
                blueToothSerial.print("0");
                frontCounter = 0;
            }
            
            if (frontFlag == 1) {
                blueToothSerial.print("0");
            }
            frontFlag = 0;
        }
        
        if (rightVal < 300) {
            if (rightCounter > 1000) {
                blueToothSerial.print("4");
                Serial.print("4");
                rightCounter = 0;
            }
            
            if (rightFlag == 1) {
                blueToothSerial.print("4");
            }
            rightFlag = 0;
        }
        
        if (leftVal < 300) {
            if (leftCounter > 1000) {
                blueToothSerial.print("6");
                leftCounter = 0;
            }
            
            if (leftFlag == 1) {
                blueToothSerial.print("6");
            }
            leftFlag = 0;
        }

        if(blueToothSerial.available()) {
            recvChar = blueToothSerial.read();
            switch (recvChar) {
                case 'A':
                   autoLeftFlag = 0;
                   autoRightFlag = 0;
                   if (autopilot == 0) {
                         
                       autopilot = 1;
                       if (frontFlag == 0 && leftFlag == 0 && rightFlag == 0) {
                            goForward(1);
                       } else if (backFlag == 0) {
                            reverse(1);
                       }
                   } else {
                       autopilot = 0;
                       brake();
                   }
                  
                   break;
                // STRAIGHT
                case 'b':
                    brake();
                    break;
                    
                case 'g':
                    if (frontFlag == 0 && leftFlag == 0 && rightFlag == 0 && disable == 0) {
                        goForward(1);
                    }
                    break;
                case 'r':
                    if (backFlag == 0 && disable == 0) {
                        reverse(1);
                    } 
                    break;
                case 'p':
                    if (backFlag == 0) {
                        reverse(0);
                    }
                    break;
                    
                    
                // LEFT:
                case 'l'://a
                    if (frontFlag == 0 && leftFlag == 0  && rightFlag == 0  && disable == 0) { 
                        goLeft();
                    }
                    break;
                    
                    
                // RIGHT:
                case 'i': // 'u'
                    if (frontFlag == 0 && rightFlag == 0 && leftFlag == 0  && disable == 0) { 
                        goRight();
                    }
                    break;
                    
                case 'o':
                    if (lightOn == 0) {
                        digitalWrite(5, HIGH);
                        lightOn = 1;
                    } else {
                        digitalWrite(5, LOW);
                        lightOn = 0;
                    }
                    break;
                case '9':
                    if (disable == 1) {
                        disable = 0;
                    } else {
                        disable = 1;
                    }
            }
        }
   
        if (Serial.available()) {
            recvChar = Serial.read();
        }
   }
}

void goRight() {
    turnFlag    = 1;
    turnCounter = 0;
    left        = 50;
    right       = 150;
    reverseFlag = 0;
    digitalWrite(12, HIGH);
    digitalWrite(9, LOW);
    analogWrite(3, left);
    
    digitalWrite(13, HIGH);
    digitalWrite(8, LOW);
    analogWrite(11, right);
}


void goLeft() {
    turnFlag    = 1;
    turnCounter = 0;
    left        = 138;
    right       = 50;
    reverseFlag = 0;
    digitalWrite(12, HIGH);
    digitalWrite(9, LOW);
    analogWrite(3, left);
    
    digitalWrite(13, HIGH);
    digitalWrite(8, LOW);
    analogWrite(11, right);
}

void stopForward() {
    left  = 0;
    right = 0;
    digitalWrite(12, HIGH);
    digitalWrite(9, LOW);
    analogWrite(3, left);
    
    digitalWrite(13, HIGH);
    digitalWrite(8, LOW);
    analogWrite(11, right);
}

void goForward(int fast) {
    turnFlag = 0;
    if (fast == 1) {
      left  = 118;
      right = 130;
    } else {
      left  = 255;
      right = 255;
      
    }
    reverseFlag = 0;
    digitalWrite(12, HIGH);
    digitalWrite(9,  LOW);
    analogWrite(3, left);
    
    digitalWrite(13, HIGH);
    digitalWrite(8,  LOW);
    analogWrite(11, right);
}

void reverse(int fast) {
    turnFlag = 0;
    reverseCounter = 0;
    if (fast == 1) {
        left  = 118;
        right = 130;
    } else {
        left  = 255;
        right = 255;
    }
    reverseFlag = 1;
    digitalWrite(12, LOW);
    digitalWrite(9,  LOW);
    analogWrite(3, left);
    
    digitalWrite(13, LOW);
    digitalWrite(8,  LOW);
    analogWrite(11, right);
      
}

void brake() {
    digitalWrite(9, HIGH);
    digitalWrite(8, HIGH);
}

void unbrake() {
    digitalWrite(9, LOW);
    digitalWrite(8, LOW);
}

void setupBlueToothConnection() {
    blueToothSerial.begin(38400); 
    blueToothSerial.print("\r\n+STWMOD=0\r\n"); 
    blueToothSerial.print("\r\n+STNA=CSC372_Project_105\r\n");
    blueToothSerial.print("\r\n+STOAUT=1\r\n");
    blueToothSerial.print("\r\n+STAUTO=0\r\n");
    
    delay(2000); 
    
    blueToothSerial.print("\r\n+INQ=1\r\n");
    
    delay(2000); 
    
    blueToothSerial.flush();
}

