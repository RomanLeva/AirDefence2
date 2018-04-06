/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package airdefence;

/**
 *
 * @author user
 */
interface Listener {
    public void beginListening(HeadQuarters hq);

    public boolean isReady();
}
