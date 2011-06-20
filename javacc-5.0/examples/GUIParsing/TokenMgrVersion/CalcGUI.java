/* Copyright (c) 2006, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */


import java.awt.*;

public class CalcGUI extends Frame implements CalcInputParserConstants {

  /**
   * A button object is created for each calculator button.  Since
   * there is going to be only one calculator GUI, these objects can
   * be static.
   */
  static Button one = new Button("1");
  static Button two = new Button("2");
  static Button three = new Button("3");
  static Button four = new Button("4");
  static Button five = new Button("5");
  static Button six = new Button("6");
  static Button seven = new Button("7");
  static Button eight = new Button("8");
  static Button nine = new Button("9");
  static Button zero = new Button("0");
  static Button dot = new Button(".");
  static Button equal = new Button("=");
  static Button add = new Button("+");
  static Button sub = new Button("-");
  static Button mul = new Button("*");
  static Button div = new Button("/");
  static Button quit = new Button("QUIT");

  /**
   * The display window with its initial setting.
   */
  static Label display = new Label("0 ");

  /**
   * The class that collects the characters produced by the GUI
   */
  static final CharCollector collector = new CharCollector();

  static final CharStream getCollector()
  {
     return collector;
  }

  /**
   * string that holds what the calculator displays
   */
  static String label = "0";

  /**
   * flag to indicate if this is the first digit.
   */
  static boolean firstDigit = true;

  /**
   * Indicates an error has occured
   */
  static boolean error = false;

  /**
   * Constructor that creates the full GUI.  This is called by the
   * main program to create one calculator GUI.
   */
  public CalcGUI() {

    super("Calculator");

    GridBagLayout gb = new GridBagLayout();
    setLayout(gb);
    GridBagConstraints gbc = new GridBagConstraints();

    display.setFont(new Font("TimesRoman", Font.BOLD, 18));
    display.setAlignment(Label.RIGHT);
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    gb.setConstraints(display, gbc);
    add(display);

    Panel buttonPanel = new Panel();
    buttonPanel.setFont(new Font("TimesRoman", Font.BOLD, 14));
    buttonPanel.setLayout(new GridLayout(4,4));
    buttonPanel.add(one); buttonPanel.add(two); buttonPanel.add(three); buttonPanel.add(four);
    buttonPanel.add(five); buttonPanel.add(six); buttonPanel.add(seven); buttonPanel.add(eight);
    buttonPanel.add(nine); buttonPanel.add(zero); buttonPanel.add(dot);  buttonPanel.add(equal);
    buttonPanel.add(add); buttonPanel.add(sub); buttonPanel.add(mul); buttonPanel.add(div);
    gbc.weighty = 1.0;
    gb.setConstraints(buttonPanel, gbc);
    add(buttonPanel);

    quit.setFont(new Font("TimesRoman", Font.BOLD, 14));
    gbc.gridheight = GridBagConstraints.REMAINDER;
    gbc.weighty = 0.0;
    gb.setConstraints(quit, gbc);
    add(quit);
    pack();
    show();
  }

  /**
   * Here we just return the character that is input.
   */
  public boolean handleEvent(Event evt) {
    char c = 0;

    if (evt.id != Event.ACTION_EVENT) {
      return false;
    }

    if (evt.target == quit) {
      System.exit(0);
    }

    if (error)
    {
       if (evt.target == zero)
       {
          error = false;
          print("0");
          firstDigit = true;
          return true;
       }

       return false;
    }

    if (evt.target == equal) {
      c = '=';
      firstDigit = true;
    }
    else if (evt.target == add) {
      c = '+';
      label = "0";
      firstDigit = true;
    }
    else if (evt.target == sub) {
      c = '-';
      label = "0";
      firstDigit = true;
    }
    else if (evt.target == mul) {
      c = '*';
      label = "0";
      firstDigit = true;
    }
    else if (evt.target == div) {
      c = '/';
      label = "0";
      firstDigit = true;
    }
    else
    {
      if (firstDigit)
         label = "";
      firstDigit = false;

      if (evt.target == one) {
        c = '1';
        label += c;
      }
      else if (evt.target == two) {
        c = '2';
        label += c;
      }
      else if (evt.target == three) {
        c = '3';
        label += c;
      }
      else if (evt.target == four) {
        c = '4';
        label += c;
      }
      else if (evt.target == five) {
        c = '5';
        label += c;
      }
      else if (evt.target == six) {
        c = '6';
        label += c;
      }
      else if (evt.target == seven) {
        c = '7';
        label += c;
      }
      else if (evt.target == eight) {
        c = '8';
        label += c;
      }
      else if (evt.target == nine) {
        c = '9';
        label += c;
      }
      else if (evt.target == zero) {
        c = '0';
        label += c;
      }
      else if (evt.target == dot) {
        c = '.';
        label += ".";
      }
      else
        return false;
    }

    print(label);
    collector.put(c);
    return true;
  }

  public static void print(double value) {
    display.setText(label = Double.toString(value));
  }

  public static void print(String image) {
    display.setText(label = image);
  }

  public static void Error(String image) {
     print(image);
     Toolkit.getDefaultToolkit().beep();
     collector.Clear();
     label = "0";
     error = true;
  }

}
