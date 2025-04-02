describe('Habit Management', () => {
  beforeEach(() => {
    // Visit the habits page
    cy.visit('/habits');
    
    // Wait for data to load
    cy.get('.habit-manager').should('be.visible');
  });
  
  it('creates a new habit', () => {
    // Fill in the form
    cy.get('#habitName').type('End-to-End Test Habit');
    cy.get('#habitDescription').type('Testing habit creation with Cypress');
    cy.get('#baseReward').clear().type('50');
    
    // Submit the form
    cy.contains('button', '创建习惯').click();
    
    // Verify the habit was created
    cy.contains('.habit-card', 'End-to-End Test Habit').should('be.visible');
  });
  
  it('marks a habit as completed', () => {
    // Create a test habit first
    cy.get('#habitName').type('Completion Test Habit');
    cy.get('#baseReward').clear().type('30');
    cy.contains('button', '创建习惯').click();
    
    // Get today's date and find the corresponding calendar cell
    const today = new Date();
    const day = today.getDate();
    
    // Find the habit card and click on today's date
    cy.contains('.habit-card', 'Completion Test Habit')
      .find('.calendar-grid')
      .contains('.calendar-day', day)
      .filter('.current-month.today')
      .click();
    
    // Verify the date was marked as completed
    cy.contains('.habit-card', 'Completion Test Habit')
      .find('.calendar-grid')
      .contains('.calendar-day', day)
      .should('have.class', 'completed');
  });
  
  it('edits an existing habit', () => {
    // Create a test habit first
    cy.get('#habitName').type('Edit Test Habit');
    cy.get('#baseReward').clear().type('40');
    cy.contains('button', '创建习惯').click();
    
    // Click the edit button
    cy.contains('.habit-card', 'Edit Test Habit')
      .find('.edit-btn')
      .click();
    
    // Edit the habit
    cy.get('#editName').clear().type('Updated Habit Name');
    cy.get('#editDescription').clear().type('Updated description');
    
    // Save the changes
    cy.contains('button', '保存').click();
    
    // Verify the habit was updated
    cy.contains('.habit-card', 'Updated Habit Name').should('be.visible');
    cy.contains('.habit-card', 'Updated description').should('be.visible');
  });
  
  it('deletes a habit', () => {
    // Create a test habit first
    cy.get('#habitName').type('Delete Test Habit');
    cy.get('#baseReward').clear().type('20');
    cy.contains('button', '创建习惯').click();
    
    // Verify the habit was created
    cy.contains('.habit-card', 'Delete Test Habit').should('be.visible');
    
    // Click the delete button
    cy.contains('.habit-card', 'Delete Test Habit')
      .find('.delete-btn')
      .click();
    
    // Confirm deletion
    cy.on('window:confirm', () => true);
    
    // Verify the habit was deleted
    cy.contains('.habit-card', 'Delete Test Habit').should('not.exist');
  });
});
