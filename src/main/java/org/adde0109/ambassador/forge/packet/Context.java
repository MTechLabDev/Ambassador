package org.adde0109.ambassador.forge.packet;

public class Context {

  private final int responseID;

  private Context(int responseID) {
    this.responseID = responseID;
  }

  static Context createContext(int responseID) {
    return new Context(responseID);
  }

  static ClientContext createContext(int responseID, boolean clientSuccess) {
    return new ClientContext(responseID,clientSuccess);
  }

  public int getResponseID() {
    return responseID;
  }

  public static class ClientContext extends Context {

    private final boolean clientSuccess;
    ClientContext(int responseID, boolean clientSuccess) {
      super(responseID);
      this.clientSuccess = clientSuccess;
    }

    public boolean success() {
      return clientSuccess;
    }
  }
}
