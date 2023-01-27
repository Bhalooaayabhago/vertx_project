package org.example;

import io.reactivex.rxjava3.core.Single;

public class checker
{
    private Single<Integer> flgData;
    private Single<Integer> flgName;
    private Single<Integer> flgCoord;
    public Single<Integer> getFlgData() {
        return flgData;
    }

    public void setFlgData(Single<Integer> flgData) {
        this.flgData = flgData;
    }

    public Single<Integer> getFlgName() {
        return flgName;
    }

    public void setFlgName(Single<Integer> flgName) {
        this.flgName = flgName;
    }

    public Single<Integer> getFlgCoord() {
        return flgCoord;
    }

    public void setFlgCoord(Single<Integer> flgCoord) {
        this.flgCoord = flgCoord;
    }
}
