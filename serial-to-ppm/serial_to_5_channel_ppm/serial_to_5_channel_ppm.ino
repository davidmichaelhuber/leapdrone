#include <avr/io.h>
#include <avr/interrupt.h>

#define PPMOUT 10

//##################################################
// Variables
//##################################################
    
int frame_time = 5000;
int time_elapsed = 0;

// 175 = 1ms
// 300 = 1.5ms
// 425 = 2ms
int channels[] = {300, 300, 300, 300, 300};

int pause_constant = 75;
int current_channel = 0;
boolean is_pause = false;
boolean reset_time_elapsed = false;

byte dataInput;
boolean cmdIdReceived = false;
boolean chNrReceived = false;
int chIndex;

void setup()
{
    //##################################################
    // I/O Initialization
    //##################################################
    
    // Set PPMOUT (Pin 10) to output mode
    pinMode(PPMOUT, OUTPUT);
 
    // Set PPMOUT to HIGH
    digitalWrite(PPMOUT, HIGH);
    
    //##################################################
    // Timer1 Initialization
    //##################################################
    
    noInterrupts();
    
    TCCR1A = 0;
    TCCR1B = 0;
    
    // First frame of impulses is ignored
    OCR1A = channels[0];
    
    TCCR1B |= (1 << CS10);
    TCCR1B |= (1 << CS11);
    
    TIMSK1 |= (1 << OCIE1A);
    
    //##################################################
    // Serial Communication Initialization
    //##################################################
    
    Serial.begin(115200);
    
    interrupts();
}

void loop()
{
  if(Serial.available() > 0)
  {
    dataInput = Serial.read();
    
    if(!cmdIdReceived && dataInput == '#')
    {
      cmdIdReceived = true;
    }
    else if(cmdIdReceived)
    {
      for(int i = 0; i <= 4; i++)
      {
        if(dataInput == i + 48)
        {
          chNrReceived = true;
          chIndex = i;
        }
      }
      cmdIdReceived = false;
    }
    else if(chNrReceived)
    {
      if(dataInput >= 0 && dataInput <= 250)
      {
        channels[chIndex] = dataInput + 175;
      }
      chNrReceived = false;
    }
  }
}

ISR(TIMER1_COMPA_vect)
{
  time_elapsed += TCNT1;
  TCNT1 = 0;
  
  digitalWrite(PPMOUT, !digitalRead(PPMOUT));
  
  if(!is_pause)
  {
    is_pause = true;
    OCR1A = pause_constant;
    if(reset_time_elapsed)
    {
      time_elapsed = 0;
      reset_time_elapsed = false;
    }
  }
  else
  {
    is_pause = false;
    if(current_channel <= 4)
    {
      OCR1A = channels[current_channel];
      current_channel++;
    }
    else if(current_channel == 5)
    {
      //current_channel = 5 = SYNC
      OCR1A = frame_time - time_elapsed - pause_constant;
      current_channel = 0;
      reset_time_elapsed = true;
    }
  }
}
