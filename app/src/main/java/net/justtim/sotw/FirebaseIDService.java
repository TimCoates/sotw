package net.justtim.sotw;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static android.content.ContentValues.TAG;

/**
 * Created by tim on 10/05/17.
 */

public class FirebaseIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);


        sendRegistrationToServer(refreshedToken);
    }



    // If you want to send messages to this application instance or
    // manage this apps subscriptions on the server side, send the
    // Instance ID token to your app server.


    // TODO: Send this token back to DynamoDB
    void sendRegistrationToServer(String token) {
        return;
    }
}
