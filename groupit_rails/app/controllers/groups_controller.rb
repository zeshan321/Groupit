class GroupsController < ApplicationController
	def index
		@groups = Group.all
	end
	
	def show
		before_action :authenticate_user!
		@messages = Group.find(params[:id]).messages.all
	end
	
	def new
		before_action :authenticate_user!
		@group = Group.new
	end
	
	def create
		@group = Group.new(params.require(:group).permit(:name))
		if @group.save
			redirect_to session[:return_to]
		else
			render 'new'
		end
	end
end
