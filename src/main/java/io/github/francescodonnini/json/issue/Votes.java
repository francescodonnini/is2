
package io.github.francescodonnini.json.issue;

import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class Votes {

    @Expose
    private Boolean hasVoted;
    @Expose
    private String self;
    @Expose
    private Long votes;

    public Boolean getHasVoted() {
        return hasVoted;
    }

    public void setHasVoted(Boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public Long getVotes() {
        return votes;
    }

    public void setVotes(Long votes) {
        this.votes = votes;
    }

}
