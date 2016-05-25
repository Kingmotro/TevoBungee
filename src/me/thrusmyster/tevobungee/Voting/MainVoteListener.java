package me.thrusmyster.tevobungee.Voting;

public class MainVoteListener implements VoteListener{
	
	@Override
	public void voteMade(Vote vote) {
		
		String player = vote.getUsername();
		VoteManager.addVote(player);
		
	}
	

}
